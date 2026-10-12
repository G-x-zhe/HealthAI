package com.healthai.service.impl;

import com.healthai.entity.StudyRemindEntity;
import com.healthai.mapper.StudyRemindMapper;
import com.healthai.service.IStudyRemindService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class StudyRemindServiceImpl implements IStudyRemindService {

    @Resource
    private StudyRemindMapper remindMapper;

    //创建提醒
    @Override
    public void create(Long userId, StudyRemindEntity entity) {
        if (entity.getTitle() == null || entity.getTitle().isBlank()) {
            throw new RuntimeException("提醒标题不能为空");
        }
        if (entity.getRemindTime() == null) {
            throw new RuntimeException("提醒时间不能为空");
        }
        // 强制绑定当前用户、初始状态为未提醒，防止前端越权传入
        entity.setUserId(userId);
        entity.setStatus(0);
        if (entity.getRemindType() == null) {
            entity.setRemindType(0);   // 默认学习类型
        }
        remindMapper.insert(entity);
    }

    //我的提醒
    @Override
    public List<StudyRemindEntity> getMyReminds(Long userId) {
        return remindMapper.selectByUserId(userId);
    }

    //取消提醒
    @Override
    public void cancel(Long userId, Long remindId) {
        StudyRemindEntity remind = remindMapper.selectById(remindId);
        // 不存在或不属于当前用户，统一提示，避免通过报错差异探测他人数据
        if (remind == null || !userId.equals(remind.getUserId())) {
            throw new RuntimeException("提醒不存在或无权限");
        }
        if (remind.getStatus() != null && remind.getStatus() == 2) {
            throw new RuntimeException("该提醒已取消");
        }
        remindMapper.updateStatus(remindId, 2);
    }
}
