package com.example.aiquiz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.HashMap;
import java.nio.charset.StandardCharsets;
import com.example.aiquiz.service.LoginService;
import com.example.aiquiz.service.QuestionSetService;
import com.example.aiquiz.model.QuestionSet;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/api/questions")
public class QuestionSetController {

    @Autowired
    private LoginService loginService;
    
    @Autowired
    private QuestionSetService questionSetService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getQuestionStats(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // 获取当前用户
        String username = loginService.getUsernameByToken(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Map<String, Object> stats = questionSetService.getQuestionStats(username);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/export/{id}")
    public ResponseEntity<Resource> exportQuestionSet(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        String username = loginService.getUsernameByToken(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            String content = questionSetService.exportQuestionSet(id);
            ByteArrayResource resource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));
            
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=questionset_" + id + ".txt")
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importQuestionSet(
        @RequestParam("file") MultipartFile file,
        HttpServletRequest request
    ) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        String username = loginService.getUsernameByToken(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            QuestionSet questionSet = questionSetService.importQuestionSet(file);
            Map<String, Object> response = new HashMap<>();
            response.put("id", questionSet.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/sets")
    public ResponseEntity<List<QuestionSet>> getAllQuestionSets(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        String username = loginService.getUsernameByToken(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            List<QuestionSet> questionSets = questionSetService.getAllQuestionSets();
            return ResponseEntity.ok(questionSets);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/sets/{id}")
    public ResponseEntity<QuestionSet> getQuestionSet(
        @PathVariable Long id,
        HttpServletRequest request
    ) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        String username = loginService.getUsernameByToken(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            QuestionSet questionSet = questionSetService.getQuestionSet(id);
            return ResponseEntity.ok(questionSet);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/sets/{id}")
    public ResponseEntity<Void> deleteQuestionSet(
        @PathVariable Long id,
        HttpServletRequest request
    ) {
        // 获取并验证用户身份
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        String username = loginService.getUsernameByToken(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            questionSetService.deleteQuestionSet(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 