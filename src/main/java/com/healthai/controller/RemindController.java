package com.healthai.controller;

import com.healthai.common.OperationLog;
import com.healthai.common.Result;
import com.healthai.entity.StudyRemindEntity;
import com.healthai.service.IStudyRemindService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学习提醒
 * remindType：0-学习 1-复习 2-考试
 * status：0-未提醒 1-已提醒 2-已取消
 */
@RestController
@RequestMapping("/api/remind")
public class RemindController {

    @Resource
    private IStudyRemindService remindService;

    @PostMapping("/create")
    @OperationLog(module = "学习提醒", operation = "创建提醒")
    public Result<Void> create(@RequestAttribute("userId") Long userId,
                               @RequestBody StudyRemindEntity entity) {
        remindService.create(userId, entity);
        return Result.success();
    }

    @GetMapping("/my")
    @OperationLog(module = "学习提醒", operation = "我的提醒列表")
    public Result<List<StudyRemindEntity>> my(@RequestAttribute("userId") Long userId) {
        return Result.success(remindService.getMyReminds(userId));
    }

    @PutMapping("/cancel/{remindId}")
    @OperationLog(module = "学习提醒", operation = "取消提醒")
    public Result<Void> cancel(@RequestAttribute("userId") Long userId,
                               @PathVariable Long remindId) {
        remindService.cancel(userId, remindId);
        return Result.success();
    }
}
