package com.example.aiquiz.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InvitationCode {
    private Long id;
    private String code;
    private Boolean used;
    private String usedBy;
    private LocalDateTime usedTime;
    private LocalDateTime createTime;
} 