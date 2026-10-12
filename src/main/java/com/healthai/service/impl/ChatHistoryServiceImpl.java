package com.healthai.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.healthai.entity.ChatHistoryEntity;
import com.healthai.mapper.ChatHistoryMapper;
import com.healthai.service.IChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ChatHistoryServiceImpl implements IChatHistoryService {

    @Resource
    private ChatHistoryMapper chatHistoryMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /** 与 ChatServiceImpl 的短期上下文 key 前缀保持一致 */
    private static final String CONTEXT_KEY_PREFIX = "healthai:context:session:";

    //分页查询历史
    @Override
    public PageInfo<ChatHistoryEntity> getHistoryByUserId(Long userId, int pageNum, int pageSize) {
        // 1. 启动分页
        PageHelper.startPage(pageNum, pageSize);
        // 2. 紧跟查询
        List<ChatHistoryEntity> list = chatHistoryMapper.selectByUserId(userId);
        // 3. 包装分页结果
        return new PageInfo<>(list);
    }

    //按会话查询
    @Override
    public PageInfo<ChatHistoryEntity> getHistoryBySessionId(Long userId, String sessionId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ChatHistoryEntity> list = chatHistoryMapper.selectBySession(userId, sessionId);
        return new PageInfo<>(list);
    }

    //查看对话详情
    @Override
    public ChatHistoryEntity getHistoryDetail(Long chatId) {
        return chatHistoryMapper.selectById(chatId);
    }

    //删除会话（同时清除 Redis 短期上下文，避免残留记忆）
    @Override
    public void deleteSession(Long userId, String sessionId) {
        chatHistoryMapper.deleteBySession(userId, sessionId);
        stringRedisTemplate.delete(CONTEXT_KEY_PREFIX + sessionId);
    }

    /**
     * 会话记忆重建：倒序取最近 rounds 条，再反转为时间正序返回
     */
    @Override
    public List<ChatHistoryEntity> getRecentRounds(Long userId, String sessionId, int rounds) {
        List<ChatHistoryEntity> recent = chatHistoryMapper.selectRecentBySession(userId, sessionId, rounds);
        if (recent == null || recent.size() <= 1) return recent == null ? List.of() : recent;
        java.util.Collections.reverse(recent);
        return recent;
    }

    /**
     * 异步落库对话历史
     * 放在独立 Bean 中，由 ChatServiceImpl 注入调用，
     * 避免同类内部 this 调用导致 @Async 不走代理而失效
     */
    @Async
    @Override
    public void saveHistoryAsync(Long userId, String question, String answer,
                                 String sessionId, long cost) {
        try {
            ChatHistoryEntity e = new ChatHistoryEntity();
            e.setUserId(userId);
            e.setQuestion(question);
            e.setAnswer(answer);
            e.setSessionId(sessionId);
            e.setModelName("deepseek-r1");
            e.setResponseTime((int) cost);
            e.setIsCached(0);
            e.setCreateTime(LocalDateTime.now());
            chatHistoryMapper.insert(e);
        } catch (Exception ex) {
            // 异步线程异常不会抛回主线程，必须自己记录日志，否则静默失败
            log.error("对话历史异步落库失败 userId={} sessionId={}", userId, sessionId, ex);
        }
    }
}
