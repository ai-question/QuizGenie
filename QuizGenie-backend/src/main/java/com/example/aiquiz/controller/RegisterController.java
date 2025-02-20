package com.example.aiquiz.controller;

import com.example.aiquiz.dto.ApiResponse;
import com.example.aiquiz.dto.RegisterRequest;
import com.example.aiquiz.service.RegisterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class RegisterController {

    private static final Logger log = LoggerFactory.getLogger(RegisterController.class);

    @Autowired
    private RegisterService registerService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest request) {
        log.info("收到注册请求: {}", request.getUsername()); // 不要记录密码
        
        try {
            // 请求参数验证
            if (request == null) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(400, "请求数据不能为空", null));
            }
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(400, "用户名不能为空", null));
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(400, "密码不能为空", null));
            }

            registerService.register(request);
            return ResponseEntity.ok(new ApiResponse(200, "注册成功", null));
        } catch (Exception e) {
            log.error("注册失败", e);
            return ResponseEntity.badRequest()
                .body(new ApiResponse(400, e.getMessage(), null));
        }
    }
} 