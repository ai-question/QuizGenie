package com.example.aiquiz.service;

import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;

@Service
public class LogoutService {
    
    // 存储已失效的token
    private static final Set<String> invalidatedTokens = new HashSet<>();
    
    public void logout(String token) {
        // 将token加入失效集合
        invalidatedTokens.add(token);
    }
    
    public boolean isTokenInvalidated(String token) {
        return invalidatedTokens.contains(token);
    }
} 