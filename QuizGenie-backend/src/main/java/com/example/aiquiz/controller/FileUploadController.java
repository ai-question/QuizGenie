package com.example.aiquiz.controller;

import com.example.aiquiz.model.Question;
import com.example.aiquiz.model.QuestionSet;
import com.example.aiquiz.service.AIService;
import com.example.aiquiz.service.QuestionSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import com.example.aiquiz.dto.QuestionCountConfig;

import java.util.List;

@RestController
@RequestMapping("/api/upload")
@Slf4j
public class FileUploadController {
    
    @Autowired
    private QuestionSetService questionSetService;
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "choiceCount", required = false) Integer choiceCount,
            @RequestParam(value = "judgmentCount", required = false) Integer judgmentCount,
            @RequestParam(value = "shortAnswerCount", required = false) Integer shortAnswerCount) {
        try {
            log.debug("收到文件上传请求");
            log.debug("文件名: {}", file.getOriginalFilename());
            log.debug("选择题数量: {}", choiceCount);
            log.debug("判断题数量: {}", judgmentCount);
            log.debug("简答题数量: {}", shortAnswerCount);
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("请选择文件");
            }
            
            String fileName = file.getOriginalFilename();
            if (fileName == null) {
                return ResponseEntity.badRequest().body("文件名不能为空");
            }
            
            // 创建配置
            QuestionCountConfig config = new QuestionCountConfig();
            config.setChoiceCount(choiceCount != null ? choiceCount : 2);
            config.setJudgmentCount(judgmentCount != null ? judgmentCount : 2);
            config.setShortAnswerCount(shortAnswerCount != null ? shortAnswerCount : 1);
            
            // 生成题目集
            QuestionSet questionSet = questionSetService.createQuestionSet(file, config);
            return ResponseEntity.ok(questionSet);
            
        } catch (Exception e) {
            log.error("文件处理失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("文件处理失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/sets")
    public ResponseEntity<List<QuestionSet>> getAllQuestionSets() {
        return ResponseEntity.ok(questionSetService.getAllQuestionSets());
    }
    
    @GetMapping("/sets/{id}")
    public ResponseEntity<?> getQuestionSet(@PathVariable Long id) {
        try {
            QuestionSet questionSet = questionSetService.getQuestionSet(id);
            if (questionSet == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(questionSet);
        } catch (Exception e) {
            log.error("获取题目集失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("获取题目集失败: " + e.getMessage());
        }
    }
} 