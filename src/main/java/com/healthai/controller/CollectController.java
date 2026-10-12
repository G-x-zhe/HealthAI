package com.healthai.controller;

import com.healthai.common.OperationLog;
import com.healthai.common.Result;
import com.healthai.dto.CollectInfoDto;
import com.healthai.service.IUserCollectService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户收藏
 */
@RestController
@RequestMapping("/api/collect")
public class CollectController {

    @Resource
    private IUserCollectService collectService;

    @PostMapping("/add")
    @OperationLog(module = "用户收藏", operation = "收藏知识")
    public Result<Void> add(@RequestAttribute("userId") Long userId,
                            @RequestParam Long knowledgeId) {
        collectService.collect(userId, knowledgeId);
        return Result.success();
    }

    @DeleteMapping("/remove")
    @OperationLog(module = "用户收藏", operation = "取消收藏")
    public Result<Void> remove(@RequestAttribute("userId") Long userId,
                               @RequestParam Long knowledgeId) {
        collectService.uncollect(userId, knowledgeId);
        return Result.success();
    }

    @GetMapping("/my")
    @OperationLog(module = "用户收藏", operation = "我的收藏列表")
    public Result<List<CollectInfoDto>> my(@RequestAttribute("userId") Long userId) {
        return Result.success(collectService.getMyCollects(userId));
    }

    @GetMapping("/check")
    public Result<Map<String, Object>> check(@RequestAttribute("userId") Long userId,
                                             @RequestParam Long knowledgeId) {
        return Result.success(collectService.isCollected(userId, knowledgeId));
    }
}
