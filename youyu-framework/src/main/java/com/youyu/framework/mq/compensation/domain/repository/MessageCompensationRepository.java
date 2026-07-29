package com.youyu.framework.mq.compensation.domain.repository;

import com.youyu.framework.mq.compensation.domain.entity.MessageCompensationRecord;

import java.util.List;

/**
 * MQ 消息补偿仓储接口
 * <p>
 * 注意：各微服务需要实现此接口，提供具体的 Mapper 实现
 */
public interface MessageCompensationRepository {

    /**
     * 保存补偿记录
     *
     * @param record 补偿记录
     */
    void save(MessageCompensationRecord record);

    /**
     * 更新补偿记录
     *
     * @param record 补偿记录
     */
    void update(MessageCompensationRecord record);

    /**
     * 查询需要重试的补偿记录
     * <p>
     * 查询条件：
     * 1. status = 0 (待处理)
     * 2. next_retry_time <= now()
     * 3. 按创建时间升序排列
     *
     * @param limit 限制数量
     * @return 补偿记录列表
     */
    List<MessageCompensationRecord> findPendingCompensations(int limit);

    /**
     * 根据消息ID查询补偿记录
     *
     * @param messageId 消息ID
     * @return 补偿记录，不存在返回 null
     */
    MessageCompensationRecord findByMessageId(String messageId);
}
