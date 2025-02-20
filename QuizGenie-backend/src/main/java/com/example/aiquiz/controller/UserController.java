package com.example.aiquiz.controller;

import com.example.aiquiz.dto.ApiResponse;
import com.example.aiquiz.dto.UserInfo;
import com.example.aiquiz.dto.UserUpdateDTO;
import com.example.aiquiz.dto.PasswordUpdateDTO;
import com.example.aiquiz.service.UserService;
import com.example.aiquiz.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private LoginService loginService;
    
    @GetMapping("/info")
    public ResponseEntity<ApiResponse> getUserInfo() {
        try {
            UserInfo userInfo = userService.getCurrentUserInfo();
            return ResponseEntity.ok(new ApiResponse(200, "获取成功", userInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(400, e.getMessage(), null));
        }
    }
    
    @PostMapping("/avatar")
    public ResponseEntity<ApiResponse> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        try {
            String avatarUrl = userService.uploadAvatar(file);
            return ResponseEntity.ok(new ApiResponse(200, "上传成功", 
                Map.of("avatarUrl", avatarUrl)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(400, e.getMessage(), null));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        String username = loginService.getUsernameByToken(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Map<String, Object> stats = userService.getUserStats(username);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse> updateUserInfo(@RequestBody UserUpdateDTO userUpdateDTO) {
        try {
            UserInfo updatedInfo = userService.updateUserInfo(userUpdateDTO);
            return ResponseEntity.ok(new ApiResponse(200, "更新成功", updatedInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(400, e.getMessage(), null));
        }
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse> updatePassword(@RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        try {
            userService.updatePassword(passwordUpdateDTO);
            return ResponseEntity.ok(new ApiResponse(200, "密码修改成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(400, e.getMessage(), null));
        }
    }
} 