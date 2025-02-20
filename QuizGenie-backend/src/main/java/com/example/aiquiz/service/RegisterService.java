package com.example.aiquiz.service;

import com.example.aiquiz.dto.RegisterRequest;
import com.example.aiquiz.mapper.UserMapper;
import com.example.aiquiz.model.User;
import com.example.aiquiz.model.InvitationCode;
import com.example.aiquiz.mapper.InvitationCodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.Pattern;

@Service
public class RegisterService {
    
    private static final Logger log = LoggerFactory.getLogger(RegisterService.class);
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private InvitationCodeMapper invitationCodeMapper;
    
    public void register(RegisterRequest request) {
        // 基本参数验证
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("密码不能少于6位");
        }
        if (request.getEmail() == null || !isValidEmail(request.getEmail())) {
            throw new RuntimeException("请输入有效的邮箱地址");
        }
        if (request.getInvitationCode() == null || request.getInvitationCode().trim().isEmpty()) {
            throw new RuntimeException("邀请码不能为空");
        }
        
        // 验证邀请码
        InvitationCode invitationCode = invitationCodeMapper.findByCode(request.getInvitationCode());
        if (invitationCode == null) {
            throw new RuntimeException("无效的邀请码");
        }
        if (invitationCode.getUsed()) {
            throw new RuntimeException("该邀请码已被使用");
        }
        
        // 检查用户名是否已存在
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已被注册
        if (userMapper.findByEmail(request.getEmail()) != null) {
            throw new RuntimeException("该邮箱已被注册");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(request.getPassword().getBytes()));
        user.setEmail(request.getEmail());
        
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        
        try {
            // 标记邀请码为已使用
            invitationCodeMapper.markAsUsed(request.getInvitationCode(), request.getUsername(), now);
            // 插入用户数据
            userMapper.insert(user);
        } catch (Exception e) {
            log.error("注册失败", e);
            throw new RuntimeException("注册失败：" + e.getMessage());
        }
    }
    
    private boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(email).matches();
    }
} 