 package com.youyu.framework.cache.sync.config;

import java.net.UnknownHostException;
import java.util.UUID;

import com.youyu.common.util.IpUtils;
import lombok.*;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@Setter
@Getter
@ConfigurationProperties(prefix = "cache.sync")
public class CacheSyncProperties implements ApplicationContextAware {
    /**
     * 是否启用缓存同步功能
     */
    private boolean enabled = false;
    /**
     * 是否启用延时消息处理
     *  如果prefixKey都是一样的：则没必要所有服务都开启(浪费性能)，只需要开启一个服务即可
     *  如果prefixKey都是不一样的：相当与redis的key空间隔离，则必须开启，不然会导致延时消息无方被消费
     */
    private boolean enableDelayedHandler = true;

    private int delayedHandlerTimeoutMs = 2000;

    /**
     * Redis Stream 键名前缀
     */
    private String prefixKey = "cache:sync";
    
    /**
     * 消费者组前缀，
     *  为空时使用实例ID作为消费者组名
     *  否则，将 prefixKey + instanceId 作为消费者组名
     *
     */
    private String consumerGroupPrefix = "";
    
    /**
     * 消息超时时间（秒），超过此时间的 pending 消息会被重新认领
     * 默认 300秒 (5分钟)
     */
    private long messageTimeoutSeconds = 60 * 5;
    
    /**
     * 最大重试次数，超过此次数的消息将被丢弃
     */
    private int maxRetry = 3;
    
    /**
     * 批量消费消息数量
     */
    private int batchSize = 10;
    
    /**
     * 阻塞等待消息的时间（秒），避免频繁轮询
     */
    private long blockSeconds = 5;
    
    /**
     * Stream 最大长度，用于限制内存占用
     */
    private long maxLen = 10000;
    
    /**
     * 消费线程池大小（使用虚拟线程，无需过大）
     */

    private int threadPoolSize = 1;
    
    /**
     * 是否启用监控指标（Micrometer Metrics）
     */
    private boolean enableMetrics = true;
    
    /**
     * 是否自动清理离线的消费者
     */
    private boolean autoCleanOfflineConsumers = false;
    
    /**
     * 离线消费者超时时间（分钟），超过此时间的消费者会被清理
     * 默认 480分钟 (8小时)
     */
    private long offlineConsumerTimeoutMinutes = 60 * 8;
    
    /**
     * 是否启用告警功能
     */
    private boolean enableAlert = true;
    
    /**
     * 实例唯一标识，由应用名 + IP地址组成
     * transient 表示不参与序列化
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private transient String instanceId = null;
    
    /**
     * Spring 应用上下文
     * transient 表示不参与序列化
     */
    @Getter(AccessLevel.NONE)
    private transient ApplicationContext applicationContext;

	/**
	 * 获取消费者组名称
	 * 如果配置了 consumerGroupPrefix，则返回 "prefix-instanceId"
	 * 否则直接返回 instanceId
	 *
	 * @return 消费者组名称
	 */
	public String getConsumerGroup() {
        if(!StringUtils.hasLength(consumerGroupPrefix)){
           return getInstanceId();
        }else{
	        return consumerGroupPrefix + "-" + getInstanceId();
        }
    }

	/**
	 * 获取实例唯一标识
	 * 如果未初始化则自动生成
	 *
	 * @return 实例ID
	 */
	public String getInstanceId() {
        if(instanceId == null){
            instanceId = genInstanceId();
        }
        return instanceId;
    }

    /**
     * 生成实例唯一标识
     * 格式：应用名-IP地址 或 unknown-UUID（无法获取IP时）
     * 使用 synchronized 确保线程安全
     *
     * @return 生成的实例ID
     */
    public synchronized String genInstanceId() {
        if(instanceId != null){
            return instanceId;
        }
        // 生成默认实例 ID，确保唯一性
        StringBuilder sb = new StringBuilder();
        // 尝试多种方式获取应用名
        String appName = null;
        // 从 Spring Environment 获取
        if (applicationContext != null) {
            Environment env = applicationContext.getEnvironment();
            appName = env.getProperty("spring.application.name");
        }
        if (StringUtils.hasLength(appName)) {
            sb.append(appName).append("-");
        }
        try {
            // 添加 IP 地址
            sb.append(IpUtils.getLocalHost());
        } catch (UnknownHostException e) {
            // 如果无法获取 IP 地址，使用随机值
            sb.append("unknown-").append(UUID.randomUUID().toString());
        }
        instanceId = sb.toString();
        return instanceId;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
