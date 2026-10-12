package com.healthai.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatHistoryEntity {

    private Long chatId;
    private Long userId;
    private String question;
    private String answer;
    private String sessionId;
    private String modelName;
    private Integer responseTime;
    private Integer isCached;
    private LocalDateTime createTime;
}
