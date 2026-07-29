package com.youyu.framework.mq.compensation.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * MQ 消息补偿记录（领域模型）
 * <p>
 * 职责：
 * 1. 记录发送失败的 MQ 消息
 * 2. 跟踪重试次数和状态
 * 3. 支持指数退避重试策略
 */
@Data
public class MessageCompensationRecord {

    /** 主键ID */
    private Long id;

    /** 消息ID（业务唯一标识） */
    private String messageId;

    /** Topic */
    private String topic;

    /** Tag */
    private String tag;

    /** 消息体（JSON字符串） */
    private String messageBody;

    /** 重试次数 */
    private Integer retryCount;

    /** 最大重试次数 */
    private Integer maxRetryCount;

    /**
     * 状态:
     * 0 - 待处理
     * 1 - 处理中
     * 2 - 成功
     * 3 - 失败
     */
    private Integer status;

    /** 错误信息 */
    private String errorMessage;

    /** 下次重试时间 */
    private LocalDateTime nextRetryTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    // ==================== 状态常量 ====================

    public static final int STATUS_PENDING = 0;      // 待处理
    public static final int STATUS_PROCESSING = 1;   // 处理中
    public static final int STATUS_SUCCESS = 2;      // 成功
    public static final int STATUS_FAILED = 3;       // 失败

    // ==================== 工厂方法 ====================

    /**
     * 创建新的补偿记录
     *
     * @param messageId   消息ID
     * @param topic       Topic
     * @param tag         Tag
     * @param messageBody 消息体
     * @param maxRetryCount 最大重试次数
     * @return 补偿记录
     */
    public static MessageCompensationRecord create(String messageId, String topic, String tag, 
                                                    String messageBody, int maxRetryCount) {
        MessageCompensationRecord record = new MessageCompensationRecord();
        record.setMessageId(messageId);
        record.setTopic(topic);
        record.setTag(tag);
        record.setMessageBody(messageBody);
        record.setRetryCount(0);
        record.setMaxRetryCount(maxRetryCount);
        record.setStatus(STATUS_PENDING);
        record.setNextRetryTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        return record;
    }

    // ==================== 状态转换方法 ====================

    /**
     * 标记为处理中
     */
    public void markAsProcessing() {
        this.status = STATUS_PROCESSING;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 标记为成功
     */
    public void markAsSuccess() {
        this.status = STATUS_SUCCESS;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 标记为失败
     *
     * @param errorMessage 错误信息
     */
    public void markAsFailed(String errorMessage) {
        this.status = STATUS_FAILED;
        this.errorMessage = errorMessage;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 增加重试次数并计算下次重试时间（指数退避）
     * <p>
     * 重试间隔：60s, 120s, 240s, 480s, ...
     */
    public void increaseRetryCount(int baseIntervalSeconds) {
        this.retryCount++;
        // 指数退避：baseInterval * 2^(retryCount-1)
        long delaySeconds = (long) baseIntervalSeconds * (1L << (this.retryCount - 1));
        this.nextRetryTime = LocalDateTime.now().plusSeconds(delaySeconds);
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 判断是否可以重试
     *
     * @return true-可以重试，false-达到最大重试次数
     */
    public boolean canRetry() {
        return this.retryCount < this.maxRetryCount;
    }

    /**
     * 判断是否需要人工介入
     *
     * @return true-需要人工介入
     */
    public boolean needsManualIntervention() {
        return !canRetry() && this.status == STATUS_FAILED;
    }

    /**
     * 判断是否到达重试时间
     *
     * @return true-已到达重试时间
     */
    public boolean isReadyForRetry() {
        return this.status == STATUS_PENDING 
            && this.nextRetryTime != null 
            && !this.nextRetryTime.isAfter(LocalDateTime.now());
    }
}
