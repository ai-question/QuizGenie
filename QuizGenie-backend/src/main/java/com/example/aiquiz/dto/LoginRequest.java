package com.example.aiquiz.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
    
    // Getters and Setters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
} 