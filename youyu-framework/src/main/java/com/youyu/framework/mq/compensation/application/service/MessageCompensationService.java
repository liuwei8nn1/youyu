package com.youyu.framework.mq.compensation.application.service;

import com.youyu.common.util.CollectionUtil;
import com.youyu.framework.mq.compensation.config.MqCompensationProperties;
import com.youyu.framework.mq.compensation.domain.entity.MessageCompensationRecord;
import com.youyu.framework.mq.compensation.domain.repository.MessageCompensationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;

import java.util.List;

/**
 * MQ 消息补偿应用服务
 * <p>
 * 职责：
 * 1. 定时扫描待处理的补偿记录
 * 2. 重新发送 MQ 消息
 * 3. 更新补偿记录状态
 */
@Slf4j
@RequiredArgsConstructor
public class MessageCompensationService {

    private final MessageCompensationRepository compensationRepository;
    private final RocketMQTemplate rocketMQTemplate;
    private final MqCompensationProperties properties;

    /**
     * 执行补偿任务
     * <p>
     * 业务方可以：
     * 1. 使用 @Scheduled 单机定时执行（仅适用于单实例）
     * 2. 集成 XXL-Job、Quartz 等分布式任务调度
     * 3. 通过 HTTP 接口手动触发
     * <p>
     * 注意：
     * 1. 本方法使用 synchronized 保证单机线程安全
     * 2. 分布式环境下，建议外部调用方使用分布式锁（如 Redisson）避免多实例重复执行
     * 3. 如果使用分布式任务调度框架（如 XXL-Job），由其保证单实例执行即可
     */
    public synchronized void execute() {

        log.info("开始执行 MQ 消息补偿任务");

        // 查询待处理的补偿记录
        List<MessageCompensationRecord> compensations = 
            compensationRepository.findPendingCompensations(properties.getBatchSize());

        if (CollectionUtil.isEmpty(compensations)) {
            log.info("没有需要补偿的消息");
            return;
        }

        log.info("找到 {} 条待补偿消息", compensations.size());

        int successCount = 0;
        int failCount = 0;
        int manualInterventionCount = 0;

        for (MessageCompensationRecord record : compensations) {
            try {
                // 标记为处理中
                record.markAsProcessing();
                compensationRepository.update(record);

                // 重新发送 MQ 消息
                String destination = record.getTopic();
                if (record.getTag() != null && !record.getTag().isEmpty()) {
                    destination += ":" + record.getTag();
                }

                rocketMQTemplate.syncSend(
                    destination,
                    MessageBuilder.withPayload(record.getMessageBody()).build()
                );

                // 标记为成功
                record.markAsSuccess();
                compensationRepository.update(record);

                successCount++;
                log.info("补偿成功，messageId: {}", record.getMessageId());

            } catch (Exception e) {
                // 判断是否可以重试
                if (record.canRetry()) {
                    // 增加重试次数并计算下次重试时间
                    record.increaseRetryCount(properties.getRetryIntervalSeconds());
                    record.markAsFailed(e.getMessage());
                    compensationRepository.update(record);

                    failCount++;
                    log.warn("补偿失败，将重试，messageId: {}, retryCount: {}/{}",
                        record.getMessageId(), record.getRetryCount(), record.getMaxRetryCount());
                } else {
                    // 达到最大重试次数，需要人工介入
                    record.markAsFailed(e.getMessage());
                    compensationRepository.update(record);

                    manualInterventionCount++;
                    log.error("补偿超过最大重试次数，需要人工介入，messageId: {}, retryCount: {}",
                        record.getMessageId(), record.getRetryCount());
                }
            }
        }

        log.info("MQ 消息补偿任务完成，总数: {}, 成功: {}, 失败: {}, 需人工介入: {}",
            compensations.size(), successCount, failCount, manualInterventionCount);
    }
}
