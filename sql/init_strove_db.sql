-- ========================================
-- Strove AI 写作助手数据库初始化脚本
-- ========================================

-- 创建数据库
DROP DATABASE IF EXISTS strove_db;
CREATE DATABASE strove_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE strove_db;

-- ========================================
-- 1. 用户表
-- ========================================
CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_email` (`email`),
    INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ========================================
-- 2. 对话记录表（可选，用于云端保存对话历史）
-- ========================================
CREATE TABLE `conversations` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '对话记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `document_id` BIGINT COMMENT '关联文档ID',
    `message` TEXT NOT NULL COMMENT '用户消息',
    `response` TEXT COMMENT 'AI响应',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_created_at` (`created_at`),
    CONSTRAINT `fk_conversation_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对话记录表';

-- ========================================
-- 3. 文档表（可选，用于云端保存文档，当前MVP仅本地存储）
-- ========================================
CREATE TABLE `documents` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文档ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(200) NOT NULL DEFAULT '未命名文档' COMMENT '文档标题',
    `content` LONGTEXT COMMENT 'Markdown内容',
    `word_count` INT DEFAULT 0 COMMENT '字数统计',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_updated_at` (`updated_at`),
    CONSTRAINT `fk_document_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档表（可选）';

-- ========================================
-- 4. 提示词模板表（可选，当前MVP使用前端硬编码）
-- ========================================
CREATE TABLE `prompt_templates` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '模板ID',
    `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `category` VARCHAR(50) NOT NULL COMMENT '分类：工作/学习/生活',
    `template_content` TEXT NOT NULL COMMENT '模板内容（带占位符）',
    `description` VARCHAR(255) COMMENT '模板描述',
    `usage_count` INT DEFAULT 0 COMMENT '使用次数',
    `is_active` BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_category` (`category`),
    INDEX `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提示词模板表（可选）';

-- ========================================
-- 初始化数据
-- ========================================

-- 插入测试用户（密码：123456，BCrypt加密后）
-- 注意：实际生产环境中应使用更安全的密码
INSERT INTO `users` (`username`, `email`, `password`) VALUES
('admin', 'admin@strove.ai', '$2a$10$Csiz0SDomv9vdjOE/6dfbe.ofEoY0AOMvhc9MDERPqQuYh6roC2SK'),
('testuser', 'test@strove.ai', '$2a$10$Csiz0SDomv9vdjOE/6dfbe.ofEoY0AOMvhc9MDERPqQuYh6roC2SK');

-- 插入内置提示词模板（可选）
INSERT INTO `prompt_templates` (`name`, `category`, `template_content`, `description`) VALUES
('工作总结', '工作', '请帮我写一篇[时间段]的工作总结，主要内容包括：[完成的任务]、[遇到的挑战]、[未来计划]', '适用于周报、月报、年终总结等'),
('学生短文', '学习', '请帮我写一篇关于[主题]的短文，字数控制在[500]字左右，文风[真诚/活泼/严肃]', '适用于作文、随笔、日记等'),
('职场邮件', '工作', '请帮我撰写一封[类型：感谢/请假/汇报]邮件，收件人是[职位]，主要内容是[具体事项]', '适用于各类职场邮件'),
('读后感', '学习', '请帮我写一篇关于《[书名]》的读后感，重点分析[角色/主题/写作手法]', '适用于读书笔记、读后感'),
('润色文案', '通用', '请帮我润色以下文案，使其更加[专业/生动/简洁]：[原文]', '适用于各类文案优化');

-- ========================================
-- 视图：用户统计信息（可选）
-- ========================================
CREATE VIEW `user_stats` AS
SELECT 
    u.id AS user_id,
    u.username,
    u.email,
    COUNT(DISTINCT c.id) AS conversation_count,
    COUNT(DISTINCT d.id) AS document_count,
    u.created_at AS register_date
FROM users u
LEFT JOIN conversations c ON u.id = c.user_id
LEFT JOIN documents d ON u.id = d.user_id
GROUP BY u.id, u.username, u.email, u.created_at;

-- ========================================
-- 索引优化说明
-- ========================================
-- 1. users表的email和username已创建唯一索引
-- 2. conversations表按user_id和created_at创建索引，优化查询历史记录
-- 3. documents表按user_id和updated_at创建索引，优化文档列表查询
-- 4. 所有外键约束使用ON DELETE CASCADE，删除用户时自动清理关联数据

-- ========================================
-- 数据库用户权限设置（可选）
-- ========================================
-- 创建应用专用数据库用户
-- CREATE USER 'strove_app'@'localhost' IDENTIFIED BY 'your_secure_password';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON strove_db.* TO 'strove_app'@'localhost';
-- FLUSH PRIVILEGES;

-- ========================================
-- 完成提示
-- ========================================
SELECT '数据库初始化完成！' AS message;
SELECT CONCAT('数据库名称: ', DATABASE()) AS info;
SELECT COUNT(*) AS table_count FROM information_schema.tables WHERE table_schema = 'strove_db';
