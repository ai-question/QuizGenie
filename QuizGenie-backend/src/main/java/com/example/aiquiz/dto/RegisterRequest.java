package com.example.aiquiz.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "password") // 避免密码出现在日志中
public class RegisterRequest {
    private String username;
    private String password;
    private String email;  // 可选字段
    private String invitationCode;  // 新增邀请码字段
} 