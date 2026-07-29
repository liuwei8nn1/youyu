package com.youyu.framework.mq.compensation.application.provider;

import java.util.function.Consumer;

import com.youyu.framework.mq.compensation.config.MqCompensationProperties;
import com.youyu.framework.mq.compensation.domain.entity.MessageCompensationRecord;
import com.youyu.framework.mq.compensation.domain.repository.MessageCompensationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;

/**
 * 可靠消息生产者
 * <p>
 * 职责：
 * 1. 发送 MQ 消息（支持同步/异步）
 * 2. 发送失败时自动记录到补偿表
 * 3. 保证消息最终一致性
 * <p>
 * 使用建议：
 * - 高并发场景：使用 {@link #sendAsync(String, String, String, String, Consumer)} 异步发送，性能更优
 * - 低并发/强实时场景：使用 {@link #sendSync(String, String, String, String)} 同步发送
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * @Service
 * public class OrderService {
 *     @Autowired
 *     private ReliableMessageProducer messageProducer;
 *     
 *     public void createOrder(...) {
 *         // 业务逻辑...
 *         
 *         // 方式1：异步发送（推荐，性能更优）
 *         messageProducer.sendAsync(
 *             "order-topic",
 *             "create",
 *             orderId,
 *             JSON.toJSONString(orderMessage)
 *         );
 *         
 *         // 方式2：同步发送（适合低并发场景）
 *         messageProducer.sendSync(
 *             "order-topic",
 *             "create",
 *             orderId,
 *             JSON.toJSONString(orderMessage)
 *         );
 *     }
 * }
 * }
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
public class ReliableMessageProducer {

    private final RocketMQTemplate rocketMQTemplate;
    private final MessageCompensationRepository compensationRepository;
    private final MqCompensationProperties properties;

    /**
     * 同步发送消息（带自动补偿）
     * <p>
     * 特点：
     * - 阻塞等待发送结果
     * - 适合低并发或对实时性要求高的场景
     * <p>
     * 如果发送失败，会自动记录到补偿表，由定时任务重试
     *
     * @param topic       Topic
     * @param tag         Tag（可为 null）
     * @param messageId   消息ID（业务唯一标识）
     * @param messageBody 消息体（JSON字符串）
     */
    public void sendSync(String topic, String tag, String messageId, String messageBody) {
        sendSync(topic, tag, messageId, messageBody, null);
    }

    /**
     * 同步发送消息（带自动补偿和失败回调）
     * <p>
     * 特点：
     * - 阻塞等待发送结果
     * - 失败时通过回调通知业务方
     * <p>
     * 如果发送失败且未提供回调，会抛出异常
     *
     * @param topic              Topic
     * @param tag                Tag（可为 null）
     * @param messageId          消息ID（业务唯一标识）
     * @param messageBody        消息体（JSON字符串）
     * @param saveFailConsumer   保存到补偿表失败时的回调（可为 null）
     */
    public void sendSync(String topic, String tag, String messageId, String messageBody, 
                         Consumer<SendFail> saveFailConsumer) {
        String destination = topic;
        if (tag != null && !tag.isEmpty()) {
            destination += ":" + tag;
        }

        try {
            // 尝试发送消息
            rocketMQTemplate.syncSend(
                destination,
                MessageBuilder.withPayload(messageBody).build()
            );

            log.info("消息发送成功，messageId: {}, destination: {}", messageId, destination);

        } catch (Exception e) {
            log.error("消息发送失败，将记录到补偿表，messageId: {}, destination: {}", 
                messageId, destination, e);

            // 记录到补偿表
            saveToCompensationTable(messageId, topic, tag, messageBody, saveFailConsumer);
        }
    }

    /**
     * 异步发送消息（带自动补偿，推荐）
     * <p>
     * 特点：
     * - 不阻塞主线程，性能更优
     * - 适合高并发场景
     * <p>
     * 如果发送失败，会自动记录到补偿表，由定时任务重试
     *
     * @param topic       Topic
     * @param tag         Tag（可为 null）
     * @param messageId   消息ID（业务唯一标识）
     * @param messageBody 消息体（JSON字符串）
     */
    public void sendAsync(String topic, String tag, String messageId, String messageBody, Consumer<SendFail> saveFailConsumer) {
        String destination = topic;
        if (tag != null && !tag.isEmpty()) {
            destination += ":" + tag;
        }

        try {
            // 异步发送消息
            String finalDestination = destination;
            rocketMQTemplate.asyncSend(
                destination,
                MessageBuilder.withPayload(messageBody).build(),
                new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("消息异步发送成功，messageId: {}, msgId: {}", 
                            messageId, sendResult.getMsgId());
                    }

                    @Override
                    public void onException(Throwable e) {
                        log.error("消息异步发送失败，将记录到补偿表，messageId: {}, destination: {}", 
                            messageId, finalDestination, e);
                        
                        // 记录到补偿表
                        saveToCompensationTable(messageId, topic, tag, messageBody, saveFailConsumer);
                    }
                }
            );

        } catch (Exception e) {
            log.error("消息异步发送异常，将记录到补偿表，messageId: {}, destination: {}", 
                messageId, destination, e);
            
            // 记录到补偿表
            saveToCompensationTable(messageId, topic, tag, messageBody, saveFailConsumer);
        }
    }

    public static record SendFail(String messageId, String topic, String tag, String messageBody, Throwable e) {
    }


    /**
     * 保存消息到补偿表
     * <p>
     * 注意：如果保存失败，会抛出异常，业务方需要捕获并处理
     *
     * @param messageId   消息ID
     * @param topic       Topic
     * @param tag         Tag
     * @param messageBody 消息体
     * @throws RuntimeException 保存失败时抛出
     */
    private void saveToCompensationTable(String messageId, String topic, String tag, String messageBody, Consumer<SendFail> saveFailConsumer) {
        try {
            MessageCompensationRecord record = MessageCompensationRecord.create(
                messageId,
                topic,
                tag,
                messageBody,
                properties.getMaxRetryCount()
            );
            compensationRepository.save(record);

            log.info("补偿记录保存成功，messageId: {}, compensationId: {}", 
                messageId, record.getId());

        } catch (Exception saveException) {
            if(saveFailConsumer != null) {
                saveFailConsumer.accept(new SendFail(messageId, topic, tag, messageBody, saveException));
            }else {
                log.error("【严重】补偿记录保存失败，这将导致消息丢失！messageId: {}, topic: {}, tag: {}",
                        messageId, topic, tag, saveException);
                // 抛出异常，让业务方感知并处理（如回滚业务操作、触发告警等）
                throw new RuntimeException(
                        "MQ补偿记录保存失败，消息可能丢失。messageId: " + messageId +
                                ", topic: " + topic +
                                ", tag: " + tag,
                        saveException
                );
            }
        }
    }

    /**
     * 同步发送消息（不带 Tag）
     *
     * @param topic       Topic
     * @param messageId   消息ID
     * @param messageBody 消息体
     */
    public void sendSync(String topic, String messageId, String messageBody) {
        sendSync(topic, null, messageId, messageBody);
    }

    /**
     * 同步发送消息（不带 Tag，带失败回调）
     *
     * @param topic              Topic
     * @param messageId          消息ID
     * @param messageBody        消息体
     * @param saveFailConsumer   保存到补偿表失败时的回调（可为 null）
     */
    public void sendSync(String topic, String messageId, String messageBody, 
                         Consumer<SendFail> saveFailConsumer) {
        sendSync(topic, null, messageId, messageBody, saveFailConsumer);
    }

    /**
     * 异步发送消息（不带 Tag）
     *
     * @param topic       Topic
     * @param messageId   消息ID
     * @param messageBody 消息体
     */
    public void sendAsync(String topic, String messageId, String messageBody) {
        sendAsync(topic, null, messageId, messageBody, null);
    }

    /**
     * 异步发送消息（不带 Tag，带失败回调）
     *
     * @param topic              Topic
     * @param messageId          消息ID
     * @param messageBody        消息体
     * @param saveFailConsumer   保存到补偿表失败时的回调（可为 null）
     */
    public void sendAsync(String topic, String messageId, String messageBody, 
                          Consumer<SendFail> saveFailConsumer) {
        sendAsync(topic, null, messageId, messageBody, saveFailConsumer);
    }

    /**
     * 同步发送延时消息（带自动补偿）
     * <p>
     * 特点：
     * - 阻塞等待发送结果
     * - 支持 RocketMQ 延时级别
     * <p>
     * RocketMQ 默认延时级别说明：
     * 1=1s, 2=5s, 3=10s, 4=30s, 5=1m, 6=2m, 7=3m, 8=4m, 9=5m, 10=6m,
     * 11=7m, 12=8m, 13=9m, 14=10m, 15=20m, 16=30m, 17=1h, 18=2h
     * <p>
     * 如果发送失败，会自动记录到补偿表，由定时任务重试
     *
     * @param topic       Topic
     * @param tag         Tag（可为 null）
     * @param messageId   消息ID（业务唯一标识）
     * @param messageBody 消息体（JSON字符串）
     * @param delayLevel  延时级别（1-18）
     */
    public void sendSyncWithDelay(String topic, String tag, String messageId, String messageBody, int delayLevel) {
        sendSyncWithDelay(topic, tag, messageId, messageBody, delayLevel, null);
    }

    /**
     * 同步发送延时消息（带自动补偿和失败回调）
     * <p>
     * 特点：
     * - 阻塞等待发送结果
     * - 支持 RocketMQ 延时级别
     * - 失败时通过回调通知业务方
     * <p>
     * RocketMQ 默认延时级别说明：
     * 1=1s, 2=5s, 3=10s, 4=30s, 5=1m, 6=2m, 7=3m, 8=4m, 9=5m, 10=6m,
     * 11=7m, 12=8m, 13=9m, 14=10m, 15=20m, 16=30m, 17=1h, 18=2h
     * <p>
     * 如果发送失败且未提供回调，会抛出异常
     *
     * @param topic              Topic
     * @param tag                Tag（可为 null）
     * @param messageId          消息ID（业务唯一标识）
     * @param messageBody        消息体（JSON字符串）
     * @param delayLevel         延时级别（1-18）
     * @param saveFailConsumer   保存到补偿表失败时的回调（可为 null）
     */
    public void sendSyncWithDelay(String topic, String tag, String messageId, String messageBody, 
                                   int delayLevel, Consumer<SendFail> saveFailConsumer) {
        String destination = topic;
        if (tag != null && !tag.isEmpty()) {
            destination += ":" + tag;
        }

        try {
            // 尝试发送延时消息
            rocketMQTemplate.syncSend(
                destination,
                MessageBuilder.withPayload(messageBody).build(),
                3000,  // 超时时间 3 秒
                delayLevel  // 延时级别
            );

            log.info("延时消息发送成功，messageId: {}, destination: {}, delayLevel: {}", 
                messageId, destination, delayLevel);

        } catch (Exception e) {
            log.error("延时消息发送失败，将记录到补偿表，messageId: {}, destination: {}, delayLevel: {}", 
                messageId, destination, delayLevel, e);

            // 记录到补偿表
            saveToCompensationTable(messageId, topic, tag, messageBody, saveFailConsumer);
        }
    }

    /**
     * 同步发送延时消息（不带 Tag）
     *
     * @param topic       Topic
     * @param messageId   消息ID
     * @param messageBody 消息体
     * @param delayLevel  延时级别（1-18）
     */
    public void sendSyncWithDelay(String topic, String messageId, String messageBody, int delayLevel) {
        sendSyncWithDelay(topic, null, messageId, messageBody, delayLevel);
    }

    /**
     * 同步发送延时消息（不带 Tag，带失败回调）
     *
     * @param topic              Topic
     * @param messageId          消息ID
     * @param messageBody        消息体
     * @param delayLevel         延时级别（1-18）
     * @param saveFailConsumer   保存到补偿表失败时的回调（可为 null）
     */
    public void sendSyncWithDelay(String topic, String messageId, String messageBody, 
                                   int delayLevel, Consumer<SendFail> saveFailConsumer) {
        sendSyncWithDelay(topic, null, messageId, messageBody, delayLevel, saveFailConsumer);
    }
}
