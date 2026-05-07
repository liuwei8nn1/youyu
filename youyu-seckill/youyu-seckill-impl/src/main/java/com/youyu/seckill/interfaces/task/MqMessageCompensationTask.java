package com.youyu.seckill.interfaces.task;

import com.youyu.framework.mq.compensation.application.service.MessageCompensationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * MQ 消息补偿任务
 * <p>
 * 职责：
 * 1. 定期扫描补偿表中的失败消息
 * 2. 重新发送 MQ 消息
 * 3. 超过最大重试次数后告警人工介入
 * <p>
 * 注意：
 * 1. 此任务使用框架层的 MessageCompensationService
 * 2. 单机部署时直接使用 @Scheduled
 * 3. 分布式部署时建议使用 XXL-Job 或添加分布式锁
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(MessageCompensationService.class)
public class MqMessageCompensationTask {

    private final MessageCompensationService compensationService;

    /**
     * 每1分钟执行一次补偿任务
     * <p>
     * 补偿策略：
     * 1. 查询状态为待处理且到达重试时间的记录
     * 2. 重新发送 MQ 消息
     * 3. 成功则标记为成功，失败则增加重试次数
     * 4. 超过最大重试次数（默认3次）后标记为失败，需要人工介入
     * 5. 重试间隔采用指数退避：60s, 120s, 240s, 480s, ...
     * <p>
     * 分布式环境注意事项：
     * - 如果使用 XXL-Job 等分布式任务调度框架，由其保证单实例执行
     * - 如果使用 @Scheduled，建议添加分布式锁（如 Redisson）避免多实例重复执行
     */
    @Scheduled(fixedRate = 60 * 1000)  // 1分钟
    public void executeCompensation() {
        log.debug("开始执行 MQ 消息补偿任务");
        
        try {
            compensationService.execute();
        } catch (Exception e) {
            log.error("MQ 消息补偿任务执行异常", e);
        }
    }
}
