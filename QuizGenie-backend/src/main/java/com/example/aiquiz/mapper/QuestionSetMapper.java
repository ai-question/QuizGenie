package com.example.aiquiz.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import com.example.aiquiz.model.QuestionSet;
import java.util.List;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QuestionSetMapper {
    @Select("SELECT * FROM question_set WHERE id = #{id} AND user_id = #{userId}")
    QuestionSet findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    
    @Select("SELECT * FROM question_set WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<QuestionSet> findByUserId(Long userId);
    
    @Insert("INSERT INTO question_set (user_id, title, description, question_count, create_time, update_time) " +
            "VALUES (#{userId}, #{title}, #{description}, #{questionCount}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(QuestionSet questionSet);
} 