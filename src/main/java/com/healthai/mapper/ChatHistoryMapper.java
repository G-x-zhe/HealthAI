package com.healthai.mapper;

import com.healthai.entity.ChatHistoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatHistoryMapper {

    int insert(ChatHistoryEntity e);

    //分页查询历史
    List<ChatHistoryEntity> selectByUserId(@Param("userId") Long userId);

    //按会话查询
    List<ChatHistoryEntity> selectBySession(@Param("userId") Long userId,
                                            @Param("sessionId") String sessionId);

    //查看对话详情
    ChatHistoryEntity selectById(@Param("chatId") Long chatId);

    //查询某会话最近 N 条问答记录（倒序取出，Service 层反转成正序），用于 Redis 上下文失效后重建记忆
    List<ChatHistoryEntity> selectRecentBySession(@Param("userId") Long userId,
                                                  @Param("sessionId") String sessionId,
                                                  @Param("limit") int limit);

    //删除会话
    int deleteBySession(@Param("userId") Long userId, @Param("sessionId") String sessionId);
}
