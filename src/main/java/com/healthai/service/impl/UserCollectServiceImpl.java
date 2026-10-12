package com.healthai.service.impl;

import com.healthai.dto.CollectInfoDto;
import com.healthai.entity.MedicalKnowledgeEntity;
import com.healthai.entity.UserCollectEntity;
import com.healthai.mapper.MedicalKnowledgeMapper;
import com.healthai.mapper.UserCollectMapper;
import com.healthai.service.IUserCollectService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserCollectServiceImpl implements IUserCollectService {

    @Resource
    private UserCollectMapper collectMapper;
    @Resource
    private MedicalKnowledgeMapper knowledgeMapper;

    //收藏
    @Override
    public void collect(Long userId, Long knowledgeId) {
        // 1. 知识必须存在，避免收藏不存在的内容
        MedicalKnowledgeEntity knowledge = knowledgeMapper.selectById(knowledgeId);
        if (knowledge == null) {
            throw new RuntimeException("知识不存在");
        }
        // 2. 查重，依赖唯一索引 uk_user_knowledge 兜底
        if (collectMapper.countByUserAndKnowledge(userId, knowledgeId) > 0) {
            throw new RuntimeException("已收藏过该知识");
        }
        UserCollectEntity entity = new UserCollectEntity();
        entity.setUserId(userId);
        entity.setKnowledgeId(knowledgeId);
        collectMapper.insert(entity);
    }

    //取消收藏
    @Override
    public void uncollect(Long userId, Long knowledgeId) {
        collectMapper.deleteByUserAndKnowledge(userId, knowledgeId);
    }

    /**
     * 我的收藏列表
     * 两步查询解决 N+1：先一次查收藏记录，再用一条 IN SQL 批量查知识，最后内存组装
     */
    @Override
    public List<CollectInfoDto> getMyCollects(Long userId) {
        // 1. 一次查出该用户所有收藏
        List<UserCollectEntity> collects = collectMapper.selectByUserId(userId);
        if (collects == null || collects.isEmpty()) {
            return List.of();
        }

        // 2. 收集所有 knowledgeId，一条 IN SQL 批量查询
        List<Long> ids = collects.stream()
                .map(UserCollectEntity::getKnowledgeId)
                .collect(Collectors.toList());
        Map<Long, MedicalKnowledgeEntity> map = knowledgeMapper.selectByIds(ids).stream()
                .collect(Collectors.toMap(MedicalKnowledgeEntity::getKnowledgeId, k -> k));

        // 3. 内存组装 DTO
        List<CollectInfoDto> result = new ArrayList<>();
        for (UserCollectEntity c : collects) {
            MedicalKnowledgeEntity k = map.get(c.getKnowledgeId());
            if (k == null) continue;    // 知识被删除时跳过该条收藏
            CollectInfoDto dto = new CollectInfoDto();
            dto.setCollectId(c.getCollectId());
            dto.setKnowledgeId(k.getKnowledgeId());
            dto.setTitle(k.getTitle());
            dto.setCategory(k.getCategory());
            dto.setCreateTime(c.getCreateTime() == null ? null : c.getCreateTime().toString());
            result.add(dto);
        }
        return result;
    }

    //检查是否已收藏
    @Override
    public Map<String, Object> isCollected(Long userId, Long knowledgeId) {
        boolean collected = collectMapper.countByUserAndKnowledge(userId, knowledgeId) > 0;
        return Map.of("collected", collected);
    }
}
