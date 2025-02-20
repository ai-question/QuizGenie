package com.example.aiquiz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.aiquiz.service.QuizService;
import com.example.aiquiz.dto.ApiResponse;

import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {
    @Autowired
    private QuizService quizService;
    
    @PostMapping("/record")
    public ResponseEntity<ApiResponse> recordQuizResult(@RequestBody Map<String, Object> result) {
        try {
            // 验证必要参数
            if (!result.containsKey("questionSetId") || result.get("questionSetId") == null) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(400, "题目集ID不能为空", null));
            }
            if (!result.containsKey("correctCount") || result.get("correctCount") == null) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(400, "正确题目数不能为空", null));
            }
            if (!result.containsKey("totalCount") || result.get("totalCount") == null) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(400, "总题目数不能为空", null));
            }

            Long questionSetId = Long.parseLong(String.valueOf(result.get("questionSetId")));
            Integer correctCount = (Integer) result.get("correctCount");
            Integer totalCount = (Integer) result.get("totalCount");
            
            quizService.recordQuizResult(questionSetId, correctCount, totalCount);
            return ResponseEntity.ok(new ApiResponse(200, "记录成功", null));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(400, "题目集ID格式错误", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(400, e.getMessage(), null));
        }
    }
} 