-- 创建题目集合表（如果不存在）
CREATE TABLE IF NOT EXISTS question_set (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '创建者用户ID',
    title VARCHAR(255) NOT NULL COMMENT '标题',
    description TEXT COMMENT '文档描述',
    question_count INT NOT NULL COMMENT '题目数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES user(id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目集合表';

-- 创建题目表（如果不存在）
CREATE TABLE IF NOT EXISTS question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    set_id BIGINT NOT NULL COMMENT '关联的题目集合ID',
    question_number INT NOT NULL COMMENT '题号',
    type VARCHAR(50) NOT NULL COMMENT '题目类型：选择题/判断题/简答题',
    content TEXT NOT NULL COMMENT '题目内容',
    answer TEXT NOT NULL COMMENT '答案',
    analysis TEXT COMMENT '解析',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (set_id) REFERENCES question_set(id) ON DELETE CASCADE,
    INDEX idx_set_id (set_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目表';

-- 创建用户表（如果不存在）
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入默认管理员账号
INSERT INTO user (username, password) 
SELECT 'admin', 'admin123'
WHERE NOT EXISTS (SELECT * FROM user WHERE username = 'admin');

-- 创建邀请码表
CREATE TABLE IF NOT EXISTS invitation_code (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE COMMENT '邀请码',
    used BOOLEAN DEFAULT FALSE COMMENT '是否已使用',
    used_by VARCHAR(50) DEFAULT NULL COMMENT '使用者用户名',
    used_time DATETIME DEFAULT NULL COMMENT '使用时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邀请码表';

-- 处理历史数据，将现有题目集都关联到admin用户（如果有历史数据的话）
UPDATE question_set SET user_id = (SELECT id FROM user WHERE username = 'admin')
WHERE user_id IS NULL; 