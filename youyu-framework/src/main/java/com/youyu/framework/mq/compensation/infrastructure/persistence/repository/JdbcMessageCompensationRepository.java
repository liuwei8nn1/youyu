package com.youyu.framework.mq.compensation.infrastructure.persistence.repository;

import com.youyu.framework.mq.compensation.domain.entity.MessageCompensationRecord;
import com.youyu.framework.mq.compensation.domain.repository.MessageCompensationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * MQ 消息补偿仓储 JDBC 实现
 * <p>
 * 职责：
 * 1. 基于 JdbcTemplate 实现数据访问，不依赖 MyBatis-Plus
 * 2. 支持自定义表名
 * 3. 降低集成方的依赖负担
 */
@RequiredArgsConstructor
public class JdbcMessageCompensationRepository implements MessageCompensationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final String tableName;

    private static final RowMapper<MessageCompensationRecord> ROW_MAPPER = new MessageCompensationRecordRowMapper();

    @Override
    public void save(MessageCompensationRecord record) {
        String sql = "INSERT INTO " + tableName + 
            " (message_id, topic, tag, message_body, retry_count, max_retry_count, " +
            "status, error_message, next_retry_time, create_time, update_time) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        jdbcTemplate.update(sql,
            record.getMessageId(),
            record.getTopic(),
            record.getTag(),
            record.getMessageBody(),
            record.getRetryCount(),
            record.getMaxRetryCount(),
            record.getStatus(),
            record.getErrorMessage(),
            record.getNextRetryTime(),
            record.getCreateTime(),
            record.getUpdateTime()
        );
        
        // 注意：如果需要回写 ID，可以使用 KeyHolder
        // 但当前领域模型中 ID 由业务方生成（如雪花算法），所以不需要回写
    }

    @Override
    public void update(MessageCompensationRecord record) {
        String sql = "UPDATE " + tableName + 
            " SET status = ?, error_message = ?, retry_count = ?, " +
            "next_retry_time = ?, update_time = ? WHERE id = ?";
        
        jdbcTemplate.update(sql,
            record.getStatus(),
            record.getErrorMessage(),
            record.getRetryCount(),
            record.getNextRetryTime(),
            record.getUpdateTime(),
            record.getId()
        );
    }

    @Override
    public List<MessageCompensationRecord> findPendingCompensations(int limit) {
        String sql = "SELECT * FROM " + tableName + 
            " WHERE status = ? AND next_retry_time <= ? " +
            "ORDER BY create_time ASC LIMIT ?";
        
        return jdbcTemplate.query(sql, ROW_MAPPER,
            MessageCompensationRecord.STATUS_PENDING,
            LocalDateTime.now(),
            limit
        );
    }

    @Override
    public MessageCompensationRecord findByMessageId(String messageId) {
        String sql = "SELECT * FROM " + tableName + " WHERE message_id = ?";
        
        List<MessageCompensationRecord> results = jdbcTemplate.query(sql, ROW_MAPPER, messageId);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * RowMapper 实现
     */
    private static class MessageCompensationRecordRowMapper implements RowMapper<MessageCompensationRecord> {
        @Override
        public MessageCompensationRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            MessageCompensationRecord record = new MessageCompensationRecord();
            record.setId(rs.getLong("id"));
            record.setMessageId(rs.getString("message_id"));
            record.setTopic(rs.getString("topic"));
            record.setTag(rs.getString("tag"));
            record.setMessageBody(rs.getString("message_body"));
            record.setRetryCount(rs.getInt("retry_count"));
            record.setMaxRetryCount(rs.getInt("max_retry_count"));
            record.setStatus(rs.getInt("status"));
            record.setErrorMessage(rs.getString("error_message"));
            
            java.sql.Timestamp nextRetryTime = rs.getTimestamp("next_retry_time");
            record.setNextRetryTime(nextRetryTime != null ? nextRetryTime.toLocalDateTime() : null);
            
            java.sql.Timestamp createTime = rs.getTimestamp("create_time");
            record.setCreateTime(createTime != null ? createTime.toLocalDateTime() : null);
            
            java.sql.Timestamp updateTime = rs.getTimestamp("update_time");
            record.setUpdateTime(updateTime != null ? updateTime.toLocalDateTime() : null);
            
            return record;
        }
    }
}
