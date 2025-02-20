package com.example.aiquiz.mapper;

import com.example.aiquiz.entity.QuizRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface QuizRecordMapper {
    @Insert("INSERT INTO quiz_records (user_id, question_set_id, correct_count, total_count, create_time) " +
            "VALUES (#{userId}, #{questionSetId}, #{correctCount}, #{totalCount}, #{createTime})")
    void insert(QuizRecord record);
    
    @Select("SELECT COALESCE(SUM(correct_count), 0) FROM quiz_records WHERE user_id = #{userId}")
    Integer getTotalCorrectCount(Long userId);
    
    @Select("SELECT COALESCE(SUM(total_count), 0) FROM quiz_records WHERE user_id = #{userId}")
    Integer getTotalQuestionCount(Long userId);
    
    // 获取用户的答题记录日期列表（按日期降序排序）
    @Select("SELECT DISTINCT DATE(create_time) as quiz_date " +
            "FROM quiz_records " +
            "WHERE user_id = #{userId} " +
            "ORDER BY quiz_date DESC")
    List<String> getUserQuizDates(Long userId);
    
    // 获取用户最近一次答题时间
    @Select("SELECT create_time FROM quiz_records " +
            "WHERE user_id = #{userId} " +
            "ORDER BY create_time DESC LIMIT 1")
    LocalDateTime getLastQuizTime(Long userId);
} 