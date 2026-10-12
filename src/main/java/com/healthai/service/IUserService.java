package com.healthai.service;

import com.healthai.dto.LoginDto;
import com.healthai.dto.RegisterDto;
import com.healthai.entity.UserEntity;

public interface IUserService {

        //用户注册
        Long register(RegisterDto dto);

        //用户登录
        String login(LoginDto dto);

        //用户登出
        void logout(String token);

        //获取用户信息
        UserEntity getUserInfo(Long userId);

        //修改用户信息
        void updateUserInfo(Long userId, UserEntity entity);
    }
