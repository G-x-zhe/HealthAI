package com.healthai.common;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BloomFilterUtils {

    private final RedissonClient redisson;
    private RBloomFilter<String> bloomFilter;

    private static final String KEY = "healthai:bloom:answer";
    private static final long CAPACITY = 1_000_000L;   // 100 万问题
    private static final double ERROR_RATE = 0.0001;  // 误判率 0.01%

    @PostConstruct
    public void init() {
        bloomFilter = redisson.getBloomFilter(KEY);
        bloomFilter.tryInit(CAPACITY, ERROR_RATE);   // 幂等，已初始化不会重置
    }

    public void add(String question) {
        bloomFilter.add(question.toLowerCase().trim());
    }

    public boolean mightContain(String question) {
        return bloomFilter.contains(question.toLowerCase().trim());
    }

    @PreDestroy
    public void destroy() { bloomFilter = null; }
}