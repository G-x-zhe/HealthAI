package com.healthai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatDto {

    @NotBlank(message = "问题不能为空")
    private String question;

    private String sessionId;
}
