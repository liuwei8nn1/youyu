package com.youyu.starter.autoconfigure.mq;

import com.youyu.framework.context.Env;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

/**
 * RocketMQTemplate Mock 配置
 * <p>
 * 职责：
 * 1. 本地/开发环境提供 Mock RocketMQTemplate，避免依赖真实的 RocketMQ 服务
 * 2. 生产环境必须配置真实的 RocketMQTemplate
 * <p>
 * 触发条件：
 * - 没有配置真实的 RocketMQTemplate Bean
 * - 且当前环境是 local 或 dev
 * <p>
 * 使用说明：
 * - 本地开发：不需要配置 rocketmq.name-server，自动使用 Mock
 * - 生产环境：必须配置 rocketmq.name-server，否则启动失败
 */
@Slf4j
@Configuration
@ConditionalOnMissingBean(RocketMQTemplate.class)
@ConditionalOnProperty(name = "rocketmq.mock", havingValue = "true")
public class RocketMQMockConfig {

    /**
     * 本地/开发环境的 Mock RocketMQTemplate
     * <p>
     * 作用：
     * - 避免业务代码中出现 @Autowired(required = false)
     * - 本地启动时不依赖 RocketMQ 服务
     * - 所有发送操作只记录日志，不实际发送
     */
    @Bean
    @ConditionalOnMissingBean(RocketMQTemplate.class)
    public RocketMQTemplate mockRocketMQTemplate() {
        if (Env.inLocal() || Env.inDev()) {
            log.warn("【{}模式】使用 Mock RocketMQTemplate，消息不会实际发送到 RocketMQ",
                Env.CURRENT.getLabel());
            return new MockRocketMQTemplate();
        } else {
            // 生产环境不应该走到这里，如果走到了说明配置有问题
            log.error("【严重】生产环境缺少 RocketMQTemplate，请检查配置！");
            throw new IllegalStateException(
                "生产环境必须配置 RocketMQTemplate。" +
                "请检查：1) rocketmq.name-server 配置是否正确；2) RocketMQ 服务是否可用"
            );
        }
    }

    /**
     * Mock RocketMQTemplate 实现
     * <p>
     * 所有发送方法都只记录日志，不实际发送
     */
    private static class MockRocketMQTemplate extends RocketMQTemplate {

        @Override
        public void asyncSend(String destination, Message<?> message, org.apache.rocketmq.client.producer.SendCallback sendCallback) {
            log.info("【Mock】异步发送消息 [destination: {}]: {}", destination, message.getPayload());
            // 模拟成功回调
            if (sendCallback != null) {
                try {
                    org.apache.rocketmq.client.producer.SendResult result =
                        new org.apache.rocketmq.client.producer.SendResult();
                    result.setMsgId("MOCK-" + System.currentTimeMillis());
                    sendCallback.onSuccess(result);
                } catch (Exception e) {
                    sendCallback.onException(e);
                }
            }
        }

        @Override
        public SendResult syncSend(String destination, Message<?> message) {
            log.info("【Mock】同步发送消息 [destination: {}]: {}", destination, message.getPayload());
            org.apache.rocketmq.client.producer.SendResult result =
                new org.apache.rocketmq.client.producer.SendResult();
            result.setMsgId("MOCK-" + System.currentTimeMillis());
            return result;
        }

        @Override
        public SendResult syncSend(String destination, Message<?> message, long timeout, int delayLevel) {
            log.info("【Mock】同步发送延时消息 [destination: {}, delayLevel: {}]: {}",
                destination, delayLevel, message.getPayload());
            org.apache.rocketmq.client.producer.SendResult result =
                new org.apache.rocketmq.client.producer.SendResult();
            result.setMsgId("MOCK-" + System.currentTimeMillis());
            return result;
        }
    }
}
