package com.healthai.service;

import com.healthai.dto.ChatDto;

import java.util.Map;

public interface IChatService {

    //智能提问
    Map<String, Object> askQuestion(Long userId, ChatDto dto);
}
