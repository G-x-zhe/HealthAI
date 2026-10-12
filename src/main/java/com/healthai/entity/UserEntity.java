package com.healthai.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserEntity {

    private Long userId;
    private String username;
    private String password;
    private String phone;
    private String email;
    private String avatar;
    private Integer role;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
