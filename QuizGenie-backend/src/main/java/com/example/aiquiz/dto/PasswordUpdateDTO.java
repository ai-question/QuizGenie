package com.example.aiquiz.dto;

import lombok.Data;

@Data
public class PasswordUpdateDTO {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
} 