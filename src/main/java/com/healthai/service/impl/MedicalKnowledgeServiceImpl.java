package com.healthai.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.healthai.entity.MedicalKnowledgeEntity;
import com.healthai.mapper.MedicalKnowledgeMapper;
import com.healthai.service.IMedicalKnowledgeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MedicalKnowledgeServiceImpl implements IMedicalKnowledgeService {

    @Resource
    private MedicalKnowledgeMapper medicalKnowledgeMapper;

    // 搜索（分页由 PageHelper 接管 XML 查询）
    public PageInfo<MedicalKnowledgeEntity> search(String keyword, String category,
                                                   int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<MedicalKnowledgeEntity> list = medicalKnowledgeMapper.search(keyword, category);
        return new PageInfo<>(list);
    }

    // 详情：查内容 + 浏览量原子 +1
    public MedicalKnowledgeEntity getKnowledgeDetail(Long id) {
        medicalKnowledgeMapper.incrementViewCount(id);
        return medicalKnowledgeMapper.selectById(id);
    }

    // 分类列表固定 5 类
    public List<String> getCategories() {
        return List.of("药品", "疾病", "症状", "检查", "广告法规");
    }
}
