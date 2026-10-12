package com.healthai.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserCollectEntity {
    private Long collectId;
    private Long userId;
    private Long knowledgeId;
    private LocalDateTime createTime;
}
