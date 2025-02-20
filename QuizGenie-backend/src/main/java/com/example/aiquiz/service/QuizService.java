package com.example.aiquiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import com.example.aiquiz.mapper.QuizRecordMapper;
import com.example.aiquiz.entity.QuizRecord;
import com.example.aiquiz.model.User;

@Service
public class QuizService {
    @Autowired
    private QuizRecordMapper quizRecordMapper;
    
    @Autowired
    private UserService userService;
    
    // 添加记录答题结果的方法
    public void recordQuizResult(Long questionSetId, Integer correctCount, Integer totalCount) {
        // 验证参数
        if (questionSetId == null) {
            throw new IllegalArgumentException("题目集ID不能为空");
        }
        if (correctCount == null) {
            throw new IllegalArgumentException("正确题目数不能为空");
        }
        if (totalCount == null) {
            throw new IllegalArgumentException("总题目数不能为空");
        }
        if (correctCount < 0 || correctCount > totalCount) {
            throw new IllegalArgumentException("正确题目数必须在0到总题目数之间");
        }
        
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        QuizRecord record = new QuizRecord();
        record.setUserId(currentUser.getId());
        record.setQuestionSetId(questionSetId);
        record.setCorrectCount(correctCount);
        record.setTotalCount(totalCount);
        record.setCreateTime(LocalDateTime.now());
        
        quizRecordMapper.insert(record);
    }
} 