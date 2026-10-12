package com.healthai.controller;

import com.healthai.common.JwtUtils;
import com.healthai.common.OperationLog;
import com.healthai.common.Result;
import com.healthai.dto.LoginDto;
import com.healthai.dto.RegisterDto;
import com.healthai.entity.UserEntity;
import com.healthai.service.IUserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private IUserService userService;

    @PostMapping("/register")
    @OperationLog(module = "用户认证", operation = "用户注册")
    public Result<Long> register(@Valid @RequestBody RegisterDto dto) {
        return Result.success(userService.register(dto));
    }

    @PostMapping("/login")
    @OperationLog(module = "用户认证", operation = "用户登录")
    public Result<String> login(@Valid @RequestBody LoginDto dto) {
        return Result.success(userService.login(dto));
    }

    @GetMapping("/info")
    @OperationLog(module = "用户认证", operation = "获取用户信息")
    public Result<UserEntity> info(@RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = JwtUtils.getUserId(token);
        if (userId == null) return Result.error(401, "未登录或登录已过期");
        return Result.success(userService.getUserInfo(userId));
    }

    @PostMapping("/logout")
    @OperationLog(module = "用户认证", operation = "用户登出")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        userService.logout(token);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "用户认证", operation = "更新用户信息")
    public Result<Void> update(@RequestAttribute("userId") Long userId,
                               @RequestBody UserEntity entity) {
        userService.updateUserInfo(userId, entity);
        return Result.success();
    }

}
