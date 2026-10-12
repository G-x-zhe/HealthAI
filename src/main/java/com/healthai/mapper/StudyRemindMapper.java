package com.healthai.mapper;

import com.healthai.entity.StudyRemindEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudyRemindMapper {

    int insert(StudyRemindEntity entity);

    //查询某用户的提醒（XML 中固定排除已取消 status=2，按提醒时间升序）
    List<StudyRemindEntity> selectByUserId(@Param("userId") Long userId);

    StudyRemindEntity selectById(@Param("remindId") Long remindId);

    //更新提醒状态 0-未提醒 1-已提醒 2-已取消
    int updateStatus(@Param("remindId") Long remindId, @Param("status") Integer status);
}
