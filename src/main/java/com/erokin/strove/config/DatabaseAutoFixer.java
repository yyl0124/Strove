package com.erokin.strove.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 数据库自动修复工具
 * 用于解决开发过程中表结构变更导致的问题
 */
@Component
public class DatabaseAutoFixer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseAutoFixer.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseAutoFixer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        fixConversationsTable();
    }

    private void fixConversationsTable() {
        try {
            logger.info("开始检查 conversations 表结构...");

            // 检查是否存在 role 字段
            if (hasColumn("conversations", "role")) {
                logger.info("检测到旧字段 'role'，正在移除...");
                try {
                    jdbcTemplate.execute("ALTER TABLE conversations DROP COLUMN role");
                    logger.info("字段 'role' 已移除");
                } catch (Exception e) {
                    logger.warn("移除 'role' 字段失败 (可能是外键约束或索引): " + e.getMessage());
                    // 如果无法删除，尝试修改为可为空，以避免插入报错
                    jdbcTemplate.execute("ALTER TABLE conversations MODIFY COLUMN role VARCHAR(20) NULL");
                    logger.info("已将 'role' 字段修改为允许 NULL");
                }
            }

            // 检查是否存在 content 字段
            if (hasColumn("conversations", "content")) {
                logger.info("检测到旧字段 'content'，正在移除...");
                try {
                    jdbcTemplate.execute("ALTER TABLE conversations DROP COLUMN content");
                    logger.info("字段 'content' 已移除");
                } catch (Exception e) {
                    logger.warn("移除 'content' 字段失败: " + e.getMessage());
                    jdbcTemplate.execute("ALTER TABLE conversations MODIFY COLUMN content TEXT NULL");
                    logger.info("已将 'content' 字段修改为允许 NULL");
                }
            }
            
            // 检查是否存在 tokens_used 字段
            if (hasColumn("conversations", "tokens_used")) {
                logger.info("检测到旧字段 'tokens_used'，正在移除...");
                try {
                    jdbcTemplate.execute("ALTER TABLE conversations DROP COLUMN tokens_used");
                    logger.info("字段 'tokens_used' 已移除");
                } catch (Exception e) {
                    logger.warn("移除 'tokens_used' 字段失败: " + e.getMessage());
                }
            }

            // 确保新字段存在
            if (!hasColumn("conversations", "message")) {
                logger.info("缺少新字段 'message'，正在添加...");
                jdbcTemplate.execute("ALTER TABLE conversations ADD COLUMN message TEXT COMMENT '用户消息'");
            }
            
            if (!hasColumn("conversations", "response")) {
                logger.info("缺少新字段 'response'，正在添加...");
                jdbcTemplate.execute("ALTER TABLE conversations ADD COLUMN response TEXT COMMENT 'AI响应'");
            }
            
            if (!hasColumn("conversations", "document_id")) {
                logger.info("缺少新字段 'document_id'，正在添加...");
                jdbcTemplate.execute("ALTER TABLE conversations ADD COLUMN document_id BIGINT COMMENT '关联文档ID'");
            }

            logger.info("conversations 表结构检查/修复完成");

        } catch (Exception e) {
            logger.error("数据库自动修复失败: " + e.getMessage());
        }
    }

    private boolean hasColumn(String tableName, String columnName) {
        try {
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                tableName, columnName
            );
            return !columns.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
