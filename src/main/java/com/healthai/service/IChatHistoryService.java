package com.healthai.service;

import com.github.pagehelper.PageInfo;
import com.healthai.entity.ChatHistoryEntity;

import java.util.List;

public interface IChatHistoryService {

    //分页查询历史
    PageInfo<ChatHistoryEntity> getHistoryByUserId(Long userId, int pageNum, int pageSize);

    //按会话查询
    PageInfo<ChatHistoryEntity> getHistoryBySessionId(Long userId, String sessionId, int pageNum, int pageSize);

    //查看对话详情
    ChatHistoryEntity getHistoryDetail(Long chatId);

    //删除会话
    void deleteSession(Long userId, String sessionId);

    /**
     * 查询某会话最近 rounds 轮问答（时间正序），用于 Redis 上下文过期后从 MySQL 重建会话记忆
     */
    List<ChatHistoryEntity> getRecentRounds(Long userId, String sessionId, int rounds);

    /**
     * 异步保存对话历史（独立 Bean 方法，确保 @Async 经代理生效）
     */
    void saveHistoryAsync(Long userId, String question, String answer,
                          String sessionId, long cost);
}
