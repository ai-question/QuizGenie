package com.example.aiquiz.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String username;
    private String avatar;
    
    public LoginResponse() {}
    
    public LoginResponse(String token, String username, String avatar) {
        this.token = token;
        this.username = username;
        this.avatar = avatar;
    }
} 