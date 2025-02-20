package com.example.aiquiz.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfo {
    private String username;
    private String email;
    private String avatar;
    private String joinDate;
} 