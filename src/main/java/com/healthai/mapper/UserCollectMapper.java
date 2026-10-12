package com.healthai.mapper;

import com.healthai.entity.UserCollectEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserCollectMapper {

    int insert(UserCollectEntity entity);

    //统计某用户是否已收藏某知识（查重/检查收藏状态）
    int countByUserAndKnowledge(@Param("userId") Long userId,
                                @Param("knowledgeId") Long knowledgeId);

    //取消收藏
    int deleteByUserAndKnowledge(@Param("userId") Long userId,
                                 @Param("knowledgeId") Long knowledgeId);

    //查询某用户的全部收藏（时间倒序）
    List<UserCollectEntity> selectByUserId(@Param("userId") Long userId);
}
