package com.healthai.dto;

import lombok.Data;

/**
 * 我的收藏列表展示对象（收藏记录 + 知识摘要的组装结果）
 */
@Data
public class CollectInfoDto {

    /** 收藏记录 ID */
    private Long collectId;

    /** 知识 ID */
    private Long knowledgeId;

    /** 知识标题 */
    private String title;

    /** 知识分类 */
    private String category;

    /** 收藏时间（字符串，便于前端直接展示） */
    private String createTime;
}
