package com.healthai.controller;

import com.github.pagehelper.PageInfo;
import com.healthai.common.OperationLog;
import com.healthai.common.Result;
import com.healthai.entity.MedicalKnowledgeEntity;
import com.healthai.service.IMedicalKnowledgeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
public class MedicalKnowledgeController {

    @Resource
    private IMedicalKnowledgeService medicalKnowledgeService;

    @OperationLog(module = "知识库", operation = "搜索医药知识")
    @GetMapping("/search")
    public Result<PageInfo<MedicalKnowledgeEntity>> search(@RequestParam(required = false) String keyword,
                                                           @RequestParam(required = false) String category,
                                                           @RequestParam(defaultValue = "1") int pageNum,
                                                           @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(medicalKnowledgeService.search(keyword, category, pageNum, pageSize));
    }

    @OperationLog(module = "知识库", operation = "查看知识详情")
    @GetMapping("/detail/{knowledgeId}")
    public Result<MedicalKnowledgeEntity> getDetail(@PathVariable Long knowledgeId) {
        return Result.success(medicalKnowledgeService.getKnowledgeDetail(knowledgeId));
    }

    @OperationLog(module = "知识库", operation = "获取分类列表")
    @GetMapping("/categories")
    public Result<List<String>> getCategories() {
        return Result.success(medicalKnowledgeService.getCategories());
    }

}
