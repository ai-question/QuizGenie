package com.example.aiquiz.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class QuizRecord {
    private Long id;
    private Long userId;
    private Long questionSetId;
    private Integer correctCount;
    private Integer totalCount;
    private LocalDateTime createTime;
} 