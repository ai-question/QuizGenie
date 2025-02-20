package com.example.aiquiz.controller;

import com.example.aiquiz.dto.ApiResponse;
import com.example.aiquiz.service.LogoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class LogoutController {

    @Autowired
    private LogoutService logoutService;

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestHeader("Authorization") String token) {
        // 从 Authorization header 中提取 token
        String actualToken = token.replace("Bearer ", "");
        
        logoutService.logout(actualToken);
        
        return ResponseEntity.ok(new ApiResponse(200, "退出成功", null));
    }
} 