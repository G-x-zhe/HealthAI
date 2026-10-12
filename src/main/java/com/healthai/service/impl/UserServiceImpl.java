package com.healthai.service.impl;

import com.healthai.common.JwtUtils;
import com.healthai.common.PasswordUtils;
import com.healthai.dto.LoginDto;
import com.healthai.dto.RegisterDto;
import com.healthai.entity.UserEntity;
import com.healthai.mapper.UserMapper;
import com.healthai.service.IUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl implements IUserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private StringRedisTemplate redis;
    private static final String BLACKLIST = "healthai:token:blacklist:";

    //用户注册
    @Override
    public Long register(RegisterDto dto) {
        // 1. 查重
        if (userMapper.countByUsername(dto.getUsername()) > 0)
            throw new RuntimeException("用户名已存在");
        // 2. MD5 加密 + 默认角色/状态
        UserEntity u = new UserEntity();
        u.setUsername(dto.getUsername());
        u.setPassword(PasswordUtils.encrypt(dto.getPassword()));
        u.setPhone(dto.getPhone());
        u.setEmail(dto.getEmail());
        u.setRole(0);
        u.setStatus(1);
        userMapper.insert(u);       // 自增主键回填到 u.userId
        return u.getUserId();
    }

    //用户登录
    @Override
    public String login(LoginDto dto) {
        UserEntity u = userMapper.findByUsername(dto.getUsername());
        if (u == null || !PasswordUtils.matches(dto.getPassword(), u.getPassword()))
            throw new RuntimeException("用户名或密码错误");
        if (u.getStatus() == 0) throw new RuntimeException("账号已被禁用");
        return JwtUtils.generateToken(u.getUserId(), u.getUsername());
    }

    //用户登出
    @Override
    public void logout(String token) {
        // JWT 无状态，靠 Redis 黑名单实现"主动失效"，TTL = token 剩余有效期
        if (token == null || JwtUtils.isExpired(token)) return;
        long remain = JwtUtils.parse(token).getExpiration().getTime() - System.currentTimeMillis();
        if (remain > 0) redis.opsForValue().set(BLACKLIST + token, "logout", remain, TimeUnit.MILLISECONDS);
    }


    //获取用户信息
    @Override
    public UserEntity getUserInfo(Long userId) {
        UserEntity u = userMapper.findById(userId);
        if (u != null) u.setPassword(null);   // 不返回密码
        return u;
    }

    //修改用户信息
    @Override
    public void updateUserInfo(Long userId, UserEntity entity) {
        entity.setUserId(userId);
        // XML 用 <set> 动态更新，只改 phone/email/avatar，密码/角色/状态传了也不会更新
        userMapper.updateById(entity);
    }
}
