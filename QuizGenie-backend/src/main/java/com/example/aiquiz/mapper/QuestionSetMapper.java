package com.example.aiquiz.mapper;

import org.apache.ibatis.annotations.*;
import com.example.aiquiz.model.QuestionSet;
import java.time.LocalDateTime;
import java.util.List;

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

    @Select("SELECT COUNT(*) FROM question_set WHERE user_id = #{userId}")
    Integer countByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM question_set WHERE user_id = #{userId} AND create_time >= #{startOfDay}")
    Integer countTodayByUserId(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay);

    @Update("UPDATE question_set SET question_count = #{count} WHERE id = #{id}")
    void updateQuestionCount(@Param("id") Long id, @Param("count") int count);

    @Select("SELECT COUNT(*) FROM question WHERE set_id IN (SELECT id FROM question_set WHERE user_id = #{userId})")
    Integer countTotalQuestionsByUserId(Long userId);

    @Select("SELECT COALESCE(AVG(CASE WHEN user_answer = answer THEN 1 ELSE 0 END), 0) " +
            "FROM user_answer WHERE user_id = #{userId}")
    Double getCorrectRateByUserId(Long userId);

    @Select("SELECT COUNT(DISTINCT DATE(answer_time)) " +
            "FROM user_answer " +
            "WHERE user_id = #{userId} " +
            "AND answer_time >= (SELECT MAX(answer_time) FROM user_answer WHERE user_id = #{userId}) - INTERVAL '30' DAY " +
            "GROUP BY DATE(answer_time) " +
            "ORDER BY DATE(answer_time) DESC")
    Integer getAnswerStreakByUserId(Long userId);
} 