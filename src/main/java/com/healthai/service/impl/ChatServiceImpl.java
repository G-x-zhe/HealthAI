package com.healthai.service.impl;

import cn.hutool.core.lang.UUID;
import com.healthai.common.BloomFilterUtils;
import com.healthai.common.SensitiveWordFilter;
import com.healthai.dto.ChatDto;
import com.healthai.entity.ChatHistoryEntity;
import com.healthai.service.IChatHistoryService;
import com.healthai.service.IChatService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ChatServiceImpl implements IChatService {

    @Resource
    private IChatHistoryService chatHistoryService;
    @Resource
    private RedissonClient redisson;
    @Resource
    private StringRedisTemplate redis;
    @Resource
    private ChatModel chatModel;                 // Spring AI 自动注入
    @Resource
    private SensitiveWordFilter sensitiveFilter;
    @Resource
    private BloomFilterUtils bloom;

    private static final int CACHE_TTL = 1800;
    private static final int RATE_WINDOW = 60;
    private static final int CONTEXT_SIZE = 10;
    private static final int CONTEXT_TTL = 7200;
    private static final String CONTEXT_KEY_PREFIX = "healthai:context:session:";


    //智能提问
    @Override
    public Map<String, Object> askQuestion(Long userId, ChatDto dto) {
        if (dto.getQuestion() == null || dto.getQuestion().isBlank())
            throw new RuntimeException("问题不能为空");

        checkRateLimit(userId);                                   // ② 限流

        // ⑦ sessionId 提前解析：无论是否命中缓存都需要它来维护会话记忆，且需回传给前端
        String sessionId = dto.getSessionId() != null && !dto.getSessionId().isBlank()
                ? dto.getSessionId() : UUID.randomUUID().toString().replace("-", "");

        String key = "healthai:answer:" + normalize(dto.getQuestion());
        // ④ 布隆预判 + 缓存
        if (bloom.mightContain(dto.getQuestion())) {
            String cached = redis.opsForValue().get(key);
            if (cached != null) {
                // 缓存命中同样计入会话记忆，避免这一轮在上下文中断档
                saveContext(sessionId, dto.getQuestion(), cached);
                return result(cached, true, 0, sessionId);
            }
        }

        // ⑤ 分布式锁
        RLock lock = redisson.getLock("healthai:submit:lock:" + userId);
        try {
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS))
                throw new RuntimeException("请求处理中，请稍后再试");

            // ⑥ 双重检查
            String cached = redis.opsForValue().get(key);
            if (cached != null) {
                saveContext(sessionId, dto.getQuestion(), cached);
                return result(cached, true, 0, sessionId);
            }

            // ⑧ 上下文：Redis 有则用；已过期则从 MySQL 最近 10 轮重建（长期会话记忆）
            String context = getContext(userId, sessionId);

            // ⑨ 调 AI
            long t0 = System.currentTimeMillis();
            String answer = callAi(dto.getQuestion(), context);
            long cost = System.currentTimeMillis() - t0;

            // ⑩ 敏感词
            answer = sensitiveFilter.filter(answer);

            // ⑪⑫⑭ 写缓存、上下文、布隆
            redis.opsForValue().set(key, answer, CACHE_TTL, TimeUnit.SECONDS);
            saveContext(sessionId, dto.getQuestion(), answer);
            bloom.add(dto.getQuestion());

            // ⑬ 异步落库（调用独立 Bean，@Async 经 Spring 代理生效）
            chatHistoryService.saveHistoryAsync(userId, dto.getQuestion(), answer, sessionId, cost);

            return result(answer, false, (int) cost, sessionId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("请求被中断");
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

    // 固定窗口限流
    private void checkRateLimit(Long userId) {
        String k = "healthai:rate:" + userId;
        Long n = redis.opsForValue().increment(k);
        if (n != null && n == 1) redis.expire(k, RATE_WINDOW, TimeUnit.SECONDS);
        if (n != null && n > 10) throw new RuntimeException("提问过于频繁，请稍后再试");
    }

    /**
     * 获取会话上下文（时间正序）：
     * 1. Redis 短期记忆命中直接使用；
     * 2. Redis 过期/缺失（隔天续聊、换设备）时，从 MySQL 捞最近 CONTEXT_SIZE 轮重建并回填，
     *    保证 AI 对历史会话仍有长期记忆。
     */
    private String getContext(Long userId, String sid) {
        String k = CONTEXT_KEY_PREFIX + sid;
        List<String> cached = redis.opsForList().range(k, 0, -1);
        if (cached != null && !cached.isEmpty()) {
            return String.join("\n", cached);
        }

        // MySQL 回捞：最近 10 轮问答（已按时间正序）
        List<ChatHistoryEntity> recent = chatHistoryService.getRecentRounds(userId, sid, CONTEXT_SIZE);
        if (recent.isEmpty()) return "";

        List<String> rounds = new ArrayList<>(recent.size());
        for (ChatHistoryEntity e : recent) {
            rounds.add("用户：" + e.getQuestion() + "\nAI：" + e.getAnswer());
        }
        // 回填 Redis，续上 TTL
        redis.opsForList().rightPushAll(k, rounds);
        redis.expire(k, CONTEXT_TTL, TimeUnit.SECONDS);
        log.info("会话记忆重建成功 userId={} sessionId={} rounds={}", userId, sid, rounds.size());
        return String.join("\n", rounds);
    }

    /**
     * 追加一轮对话到短期记忆：
     * RPUSH 保证时间正序，LTRIM 只保留最新 CONTEXT_SIZE 轮
     */
    private void saveContext(String sid, String q, String a) {
        String k = CONTEXT_KEY_PREFIX + sid;
        redis.opsForList().rightPush(k, "用户：" + q + "\nAI：" + a);
        redis.opsForList().trim(k, -CONTEXT_SIZE, -1);
        redis.expire(k, CONTEXT_TTL, TimeUnit.SECONDS);
    }

    // 调 Ollama 大模型
    private String callAi(String question, String context) {
        String system = "你是一位专业的医药健康顾问，擅长解答药品使用、疾病预防、健康养生问题。"
                + "请用通俗易懂的语言回答。回答仅供参考，不能替代专业医生的诊断和治疗建议。";
        StringBuilder prompt = new StringBuilder();
        if (context != null && !context.isBlank())
            prompt.append("【对话历史】\n").append(context).append("\n\n");
        prompt.append("【当前问题】\n").append(question).append("\n\n").append(system);

        var resp = chatModel.call(new Prompt(prompt.toString()));
        String content = resp.getResult().getOutput().getContent();
        if (content == null || content.isBlank()) throw new RuntimeException("AI 未返回内容");
        return content;
    }

    private String normalize(String q) {
        return q.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private Map<String, Object> result(String answer, boolean cached, int time, String sessionId) {
        return Map.of("answer", answer, "isCached", cached,
                "modelName", "deepseek-r1", "responseTime", time,
                "sessionId", sessionId);
    }
}
