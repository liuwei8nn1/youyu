package com.youyu.starter.autoconfigure.mq;

import com.youyu.framework.mq.compensation.application.provider.ReliableMessageProducer;
import com.youyu.framework.mq.compensation.application.service.MessageCompensationService;
import com.youyu.framework.mq.compensation.config.MqCompensationProperties;
import com.youyu.framework.mq.compensation.domain.repository.MessageCompensationRepository;
import com.youyu.framework.mq.compensation.infrastructure.persistence.repository.JdbcMessageCompensationRepository;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * MQ 补偿机制自动配置
 * <p>
 * 职责：
 * 1. 自动配置 MQ 补偿相关的 Bean
 * 2. 提供默认的 Repository 实现（基于 JdbcTemplate）
 * 3. 如果业务方已经定义了某些 Bean，则使用业务方的配置
 */
@Configuration
@ConditionalOnClass(RocketMQTemplate.class)
@EnableConfigurationProperties(MqCompensationProperties.class)
@AutoConfigureAfter({
        org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration.class,
        com.youyu.starter.autoconfigure.mq.RocketMQMockConfig.class
})
public class MqCompensationAutoConfiguration {

    /**
     * 自动配置默认的 Repository 实现
     * <p>
     * 注意：
     * 1. 只有在业务方没有自定义 Repository 时才创建
     * 2. 基于 JdbcTemplate 实现
     * 3. 支持通过配置文件自定义表名
     */
    @Bean
    @ConditionalOnMissingBean(MessageCompensationRepository.class)
    public MessageCompensationRepository messageCompensationRepository(
            JdbcTemplate jdbcTemplate,
            MqCompensationProperties properties) {
        return new JdbcMessageCompensationRepository(jdbcTemplate, properties.getTableName());
    }

    /**
     * 自动配置补偿服务
     * <p>
     * 注意：
     * 1. 需要 RocketMQTemplate 存在时才创建
     * 2. 分布式环境下，建议外部调用方使用分布式锁保证单实例执行
     */
    @Bean
    @ConditionalOnMissingBean(MessageCompensationService.class)
    @ConditionalOnBean(RocketMQTemplate.class)
    public MessageCompensationService messageCompensationService(
            MessageCompensationRepository repository,
            RocketMQTemplate rocketMQTemplate,
            MqCompensationProperties properties) {
        return new MessageCompensationService(repository, rocketMQTemplate, properties);
    }

    /**
     * 自动配置可靠消息生产者
     * <p>
     * 注意：需要 RocketMQTemplate 存在时才创建
     */
    @Bean
    @ConditionalOnMissingBean(ReliableMessageProducer.class)
    @ConditionalOnBean(RocketMQTemplate.class)
    public ReliableMessageProducer reliableMessageProducer(
            RocketMQTemplate rocketMQTemplate,
            MessageCompensationRepository repository,
            MqCompensationProperties properties) {
        return new ReliableMessageProducer(rocketMQTemplate, repository, properties);
    }
}
