package com.example.aiquiz.service;

import com.example.aiquiz.mapper.UserMapper;
import com.example.aiquiz.model.User;
import com.example.aiquiz.dto.LoginRequest;
import com.example.aiquiz.dto.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.AllArgsConstructor;

@Service
public class LoginService {
    
    @Autowired
    private UserMapper userMapper;
    
    // 存储token和用户名的映射关系
    private static final Map<String, TokenInfo> tokenMap = new ConcurrentHashMap<>();
    private static final long TOKEN_EXPIRE_TIME = 24 * 60 * 60 * 1000; // 24小时
    
    @Data
    @AllArgsConstructor
    private static class TokenInfo {
        private String username;
        private long expireTime;
    }
    
    public LoginResponse login(LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            throw new RuntimeException("用户名和密码不能为空");
        }
        
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 对输入的密码进行MD5加密后再比较
        String encryptedPassword = DigestUtils.md5DigestAsHex(request.getPassword().getBytes());
        if (!user.getPassword().equals(encryptedPassword)) {
            throw new RuntimeException("密码错误");
        }
        
        // 生成token
        String token = UUID.randomUUID().toString();
        tokenMap.put(token, new TokenInfo(user.getUsername(), 
            System.currentTimeMillis() + TOKEN_EXPIRE_TIME));
        
        return new LoginResponse(token, user.getUsername(), user.getAvatar());
    }
    
    public String getUsernameByToken(String token) {
        TokenInfo tokenInfo = tokenMap.get(token);
        if (tokenInfo != null && System.currentTimeMillis() < tokenInfo.getExpireTime()) {
            return tokenInfo.getUsername();
        }
        tokenMap.remove(token);
        return null;
    }
    
    public void logout(String token) {
        tokenMap.remove(token);
    }
} 