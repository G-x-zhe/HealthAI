package com.healthai.service;

import com.healthai.entity.StudyRemindEntity;

import java.util.List;

public interface IStudyRemindService {

    //创建学习提醒（强制绑定当前用户，状态初始化为未提醒）
    void create(Long userId, StudyRemindEntity entity);

    //我的提醒列表（不含已取消）
    List<StudyRemindEntity> getMyReminds(Long userId);

    //取消提醒（校验归属，防越权操作他人提醒）
    void cancel(Long userId, Long remindId);
}
