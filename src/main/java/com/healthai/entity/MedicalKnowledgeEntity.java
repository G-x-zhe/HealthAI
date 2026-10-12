package com.healthai.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MedicalKnowledgeEntity {

    private Long knowledgeId;
    private String title;
    private String category;
    private String content;
    private String keywords;
    private String source;
    private Integer viewCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
