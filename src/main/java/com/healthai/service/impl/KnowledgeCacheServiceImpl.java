package com.healthai.service.impl;

import com.healthai.entity.MedicalKnowledgeEntity;
import com.healthai.mapper.MedicalKnowledgeMapper;
import com.healthai.service.IKnowledgeCacheService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class KnowledgeCacheServiceImpl implements IKnowledgeCacheService {
    @Resource
    private MedicalKnowledgeMapper knowledgeMapper;

    /**
     * Redis 模板
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 知识缓存前缀
     */
    private static final String KNOWLEDGE_CACHE_PREFIX = "healthai:knowledge:";

    /**
     * 缓存预热数量（100 条）
     */
    private static final int CACHE_SIZE = 100;

    /**
     * 缓存有效期（24 小时）
     */
    private static final long CACHE_TTL_HOURS = 24;

    @Override
    public void run(ApplicationArguments args) {
        log.info("开始预热热门医药知识缓存...");
        int count = warmUpHotKnowledge();
        log.info("热门知识缓存预热完成，共缓存 {} 条知识", count);
    }

    @Override
    public void cacheKnowledge(Long knowledgeId, String content) {
        String key = KNOWLEDGE_CACHE_PREFIX + knowledgeId;
        stringRedisTemplate.opsForValue().set(key, content, CACHE_TTL_HOURS, TimeUnit.HOURS);
    }

    @Override
    public String getCachedKnowledge(Long knowledgeId) {
        String key = KNOWLEDGE_CACHE_PREFIX + knowledgeId;
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public void evictKnowledgeCache(Long knowledgeId) {
        String key = KNOWLEDGE_CACHE_PREFIX + knowledgeId;
        stringRedisTemplate.delete(key);
    }

    /**
     * 预热热门知识缓存
     * 应用启动时将浏览量最高的前 100 条知识加载到 Redis
     *
     * @return 实际预热成功的知识条数
     */
    private int warmUpHotKnowledge() {
        try {
            // 使用原生 MyBatis Mapper（XML 中实现：status=1 按浏览量倒序取前 N 条）
            List<MedicalKnowledgeEntity> hotKnowledge =
                    knowledgeMapper.selectHotList(CACHE_SIZE);

            int count = 0;
            for (MedicalKnowledgeEntity entity : hotKnowledge) {
                String key = KNOWLEDGE_CACHE_PREFIX + entity.getKnowledgeId();
                stringRedisTemplate.opsForValue()
                        .set(key, entity.getContent(), CACHE_TTL_HOURS, TimeUnit.HOURS);
                count++;
            }
            log.info("成功预热 {} 条热门知识", count);
            return count;
        } catch (Exception e) {
            log.error("缓存预热失败：{}", e.getMessage(), e);
            return 0;
        }
    }
}
