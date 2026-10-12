package com.healthai.service;

import com.github.pagehelper.PageInfo;
import com.healthai.entity.MedicalKnowledgeEntity;

import java.util.List;

public interface IMedicalKnowledgeService {

    //搜索医药知识
    PageInfo<MedicalKnowledgeEntity> search(String keyword, String category, int pageNum, int pageSize);

    //查看知识详情
    MedicalKnowledgeEntity getKnowledgeDetail(Long knowledgeId);

    //获取分类列表
    List<String> getCategories();
}
