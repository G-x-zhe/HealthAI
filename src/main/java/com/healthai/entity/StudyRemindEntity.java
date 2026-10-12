package com.healthai.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudyRemindEntity {
    private Long remindId;
    private Long userId;
    private String title;
    private String content;
    private LocalDateTime remindTime;
    private Integer remindType;
    private Integer status;
    private LocalDateTime createTime;
}
