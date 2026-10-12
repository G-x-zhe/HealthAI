package com.healthai.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * 医药知识缓存服务
 * 继承 ApplicationRunner，应用启动完成后自动执行热门知识缓存预热
 */
public interface IKnowledgeCacheService extends ApplicationRunner {

    @Override
    void run(ApplicationArguments args) throws Exception;

    void cacheKnowledge(Long knowledgeId, String content);

    String getCachedKnowledge(Long knowledgeId);

    void evictKnowledgeCache(Long knowledgeId);
}
