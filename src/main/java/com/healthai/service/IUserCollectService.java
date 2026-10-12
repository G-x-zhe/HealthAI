package com.healthai.service;

import com.healthai.dto.CollectInfoDto;

import java.util.List;
import java.util.Map;

public interface IUserCollectService {

    //收藏知识（已收藏则抛业务异常）
    void collect(Long userId, Long knowledgeId);

    //取消收藏
    void uncollect(Long userId, Long knowledgeId);

    //我的收藏列表（批量查询知识信息，避免 N+1）
    List<CollectInfoDto> getMyCollects(Long userId);

    //检查是否已收藏
    Map<String, Object> isCollected(Long userId, Long knowledgeId);
}
