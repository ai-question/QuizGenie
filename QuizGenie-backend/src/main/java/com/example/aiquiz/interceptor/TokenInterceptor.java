package com.example.aiquiz.interceptor;

import com.example.aiquiz.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class TokenInterceptor implements HandlerInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(TokenInterceptor.class);
    
    @Autowired
    private LoginService loginService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果是OPTIONS请求，放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        
        // 记录请求信息，用于调试
        log.debug("Request URI: {}", request.getRequestURI());
        log.debug("Request Method: {}", request.getMethod());
        
        // 检查是否是排除的路径
        String path = request.getRequestURI();
        if (path.startsWith("/api/login") || 
            path.startsWith("/api/register") || 
            path.startsWith("/api/upload")) {
            return true;
        }
        
        // 从请求头中获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // 验证token
        if (token != null) {
            String username = loginService.getUsernameByToken(token);
            if (username != null) {
                // 将用户信息存入SecurityContext
                request.setAttribute("username", username);
                return true;
            }
        }
        
        // token无效，返回401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
} 