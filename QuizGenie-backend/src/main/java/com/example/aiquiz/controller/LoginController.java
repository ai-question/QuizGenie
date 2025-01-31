package com.example.aiquiz.controller;

import com.example.aiquiz.dto.ApiResponse;
import com.example.aiquiz.dto.LoginRequest;
import com.example.aiquiz.dto.LoginResponse;
import com.example.aiquiz.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse loginResponse = loginService.login(request);
            return ResponseEntity.ok(new ApiResponse(200, "登录成功", loginResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(400, e.getMessage(), null));
        }
    }
}
