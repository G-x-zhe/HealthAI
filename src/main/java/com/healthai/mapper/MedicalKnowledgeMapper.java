package com.healthai.mapper;

import com.healthai.entity.MedicalKnowledgeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MedicalKnowledgeMapper {
    List<MedicalKnowledgeEntity> search(@Param("keyword") String keyword,
                                        @Param("category") String category);
    MedicalKnowledgeEntity selectById(@Param("knowledgeId") Long knowledgeId);
    int incrementViewCount(@Param("knowledgeId") Long knowledgeId);
    List<MedicalKnowledgeEntity> selectHotList(@Param("limit") int limit);
    List<MedicalKnowledgeEntity> selectByIds(@Param("ids") List<Long> ids);
}
