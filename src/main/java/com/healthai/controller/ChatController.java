package com.healthai.controller;

import com.github.pagehelper.PageInfo;
import com.healthai.common.OperationLog;
import com.healthai.common.Result;
import com.healthai.dto.ChatDto;
import com.healthai.entity.ChatHistoryEntity;
import com.healthai.service.IChatHistoryService;
import com.healthai.service.IChatService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Resource
    private IChatService chatService;
    @Resource
    private IChatHistoryService chatHistoryService;

    @PostMapping("/ask")
    @OperationLog(module = "AI 问答", operation = "智能提问")
    public Result<Map<String, Object>> ask(@RequestAttribute("userId") Long userId,
                                           @Valid @RequestBody ChatDto dto) {
        return Result.success(chatService.askQuestion(userId, dto));
    }

    @GetMapping("/history")
    @OperationLog(module = "对话历史", operation = "分页查询历史")
    public Result<PageInfo<ChatHistoryEntity>> history(@RequestAttribute("userId") Long userId,
                                                       @RequestParam(defaultValue = "1") int pageNum,
                                                       @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(chatHistoryService.getHistoryByUserId(userId, pageNum, pageSize));
    }

    @GetMapping("/history/session/{sessionId}")
    @OperationLog(module = "对话历史", operation = "按会话查询")
    public Result<PageInfo<ChatHistoryEntity>> bySession(@RequestAttribute("userId") Long userId,
                                                         @PathVariable String sessionId,
                                                         @RequestParam(defaultValue = "1") int pageNum,
                                                         @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(chatHistoryService.getHistoryBySessionId(userId, sessionId, pageNum, pageSize));
    }

    @GetMapping("/history/detail/{chatId}")
    @OperationLog(module = "对话历史", operation = "查看对话详情")
    public Result<ChatHistoryEntity> detail(@PathVariable Long chatId) {
        return Result.success(chatHistoryService.getHistoryDetail(chatId));
    }

    @DeleteMapping("/history/session/{sessionId}")
    @OperationLog(module = "对话历史", operation = "删除会话")
    public Result<?> deleteSession(@RequestAttribute("userId") Long userId,
                                   @PathVariable String sessionId) {
        chatHistoryService.deleteSession(userId, sessionId);
        return Result.success("删除成功");
    }

}
