package com.healthai.mapper;

import com.healthai.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    //信息查重
    int countByUsername(String username);

    //用户注册
    void insert(UserEntity u);

    //用户登录
    UserEntity findByUsername(String username);

    //获取用户信息
    UserEntity findById(Long userId);

    //修改用户信息
    void updateById(UserEntity entity);
}
