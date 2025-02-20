package com.example.aiquiz.service;

import com.example.aiquiz.mapper.UserMapper;
import com.example.aiquiz.model.User;
import com.example.aiquiz.dto.UserInfo;
import com.example.aiquiz.dto.UserUpdateDTO;
import com.example.aiquiz.dto.PasswordUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.time.format.DateTimeFormatter;
import com.example.aiquiz.utils.AliOSSUtils;
import java.util.HashMap;
import java.util.Map;
import com.example.aiquiz.mapper.QuestionSetMapper;
import com.example.aiquiz.utils.MD5Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.aiquiz.mapper.QuizRecordMapper;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;


@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private LoginService loginService;
    
    @Autowired
    private QuestionSetMapper questionSetMapper;
    
    @Autowired
    private HttpServletRequest request;
    
    @Autowired
    private HttpServletResponse response;
    
    @Value("${upload.path}")
    private String uploadPath;
    
    @Value("${upload.url-prefix}")
    private String urlPrefix;
    
    @Autowired
    private AliOSSUtils aliOSSUtils;
    
    @Autowired
    private QuizRecordMapper quizRecordMapper;
    
    public User getCurrentUser() {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        if (token == null) {
            return null;
        }
        
        String username = loginService.getUsernameByToken(token);
        if (username == null) {
            return null;
        }
        
        return userMapper.findByUsername(username);
    }
    
    public UserInfo getCurrentUserInfo() {
        // 从请求中获取用户名
        String username = (String) request.getAttribute("username");
        if (username == null) {
            throw new RuntimeException("用户未登录");
        }
        
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        return UserInfo.builder()
            .username(user.getUsername())
            .email(user.getEmail())
            .avatar(user.getAvatar())
            .joinDate(user.getCreateTime().format(DateTimeFormatter.ISO_LOCAL_DATE))
            .build();
    }
    
    public String uploadAvatar(MultipartFile file) throws IOException {
        // 获取当前登录用户
        String username = (String) request.getAttribute("username");
        if (username == null) {
            throw new RuntimeException("用户未登录");
        }
        
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 上传文件到阿里云OSS
        String avatarUrl = aliOSSUtils.upload(file);
        
        // 更新用户头像URL
        user.setAvatar(avatarUrl);
        userMapper.updateAvatar(user.getId(), avatarUrl);
        
        return avatarUrl;
    }
    
    public Map<String, Object> getUserStats(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        Map<String, Object> stats = new HashMap<>();
        
        // 获取总答题数和正确数
        Integer totalQuestions = quizRecordMapper.getTotalQuestionCount(user.getId());
        Integer totalCorrect = quizRecordMapper.getTotalCorrectCount(user.getId());
        
        // 计算正确率
        double correctRate = 0;
        if (totalQuestions > 0) {
            correctRate = (double) totalCorrect / totalQuestions * 100;
        }
        
        // 计算连续答题天数
        int streak = calculateStreak(user.getId());
        
        // 获取生成题目集数量
        Integer questionSets = questionSetMapper.countByUserId(user.getId());
        
        stats.put("totalQuestions", totalQuestions);
        stats.put("correctRate", Math.round(correctRate)); // 四舍五入到整数
        stats.put("streak", streak);
        stats.put("questionSets", questionSets != null ? questionSets : 0);
        
        return stats;
    }

    // 计算连续答题天数
    private int calculateStreak(Long userId) {
        List<String> quizDates = quizRecordMapper.getUserQuizDates(userId);
        if (quizDates == null || quizDates.isEmpty()) {
            return 0;
        }
        
        // 获取最后一次答题时间
        LocalDateTime lastQuizTime = quizRecordMapper.getLastQuizTime(userId);
        if (lastQuizTime == null) {
            return 0;
        }
        
        // 如果最后一次答题不是今天或昨天，连续答题中断
        LocalDateTime now = LocalDateTime.now();
        if (lastQuizTime.toLocalDate().isBefore(now.toLocalDate().minusDays(1))) {
            return 0;
        }
        
        // 计算连续天数
        int streak = 1;
        LocalDate currentDate = quizDates.get(0) != null ? 
            LocalDate.parse(quizDates.get(0)) : null;
        
        if (currentDate == null) {
            return 0;
        }
        
        for (int i = 1; i < quizDates.size(); i++) {
            LocalDate nextDate = LocalDate.parse(quizDates.get(i));
            if (currentDate.minusDays(1).equals(nextDate)) {
                streak++;
                currentDate = nextDate;
            } else {
                break;
            }
        }
        
        return streak;
    }

    public UserInfo updateUserInfo(UserUpdateDTO userUpdateDTO) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        log.info("开始更新用户信息: {}", userUpdateDTO);
        
        // 更新用户名
        if (userUpdateDTO.getUsername() != null && !userUpdateDTO.getUsername().isEmpty()) {
            // 检查用户名是否已存在
            User existingUser = userMapper.findByUsername(userUpdateDTO.getUsername());
            if (existingUser != null && !existingUser.getId().equals(currentUser.getId())) {
                throw new RuntimeException("用户名已存在");
            }
            log.info("更新用户名: {} -> {}", currentUser.getUsername(), userUpdateDTO.getUsername());
            currentUser.setUsername(userUpdateDTO.getUsername());
            int rows = userMapper.updateUsername(currentUser.getId(), userUpdateDTO.getUsername());
            if (rows != 1) {
                log.error("更新用户名失败: 影响行数={}", rows);
                throw new RuntimeException("更新用户名失败");
            }
            
            // 获取当前token
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                // 使旧token失效
                loginService.logout(token);
                // 生成新token
                String newToken = loginService.generateToken(userUpdateDTO.getUsername());
                // 将新token添加到响应头
                response.setHeader("Authorization", "Bearer " + newToken);
            }
        }
        
        // 更新邮箱
        if (userUpdateDTO.getEmail() != null && !userUpdateDTO.getEmail().isEmpty()) {
            log.info("更新邮箱: {} -> {}", currentUser.getEmail(), userUpdateDTO.getEmail());
            currentUser.setEmail(userUpdateDTO.getEmail());
            int rows = userMapper.updateEmail(currentUser.getId(), userUpdateDTO.getEmail());
            if (rows != 1) {
                log.error("更新邮箱失败: 影响行数={}", rows);
                throw new RuntimeException("更新邮箱失败");
            }
        }
        
        log.info("用户信息更新成功");
        return convertToUserInfo(currentUser);
    }

    private UserInfo convertToUserInfo(User user) {
        return UserInfo.builder()
            .username(user.getUsername())
            .email(user.getEmail())
            .avatar(user.getAvatar())
            .joinDate(user.getCreateTime().format(DateTimeFormatter.ISO_LOCAL_DATE))
            .build();
    }

    public void updatePassword(PasswordUpdateDTO passwordUpdateDTO) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        // 验证旧密码
        String oldPasswordHash = MD5Utils.encode(passwordUpdateDTO.getOldPassword());
        if (!oldPasswordHash.equals(currentUser.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        
        // 验证新密码
        if (!passwordUpdateDTO.getNewPassword().equals(passwordUpdateDTO.getConfirmPassword())) {
            throw new RuntimeException("两次输入的新密码不一致");
        }
        
        // 更新密码
        String newPasswordHash = MD5Utils.encode(passwordUpdateDTO.getNewPassword());
        userMapper.updatePassword(currentUser.getId(), newPasswordHash);
        
        // 获取当前token并清除
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            loginService.logout(token);
        }
    }
} 