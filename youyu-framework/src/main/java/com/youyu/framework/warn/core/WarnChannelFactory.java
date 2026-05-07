package com.youyu.framework.warn.core;

import com.youyu.framework.warn.config.WarnProperties;
import com.youyu.framework.warn.core.*;
import com.youyu.framework.warn.platform.DingTalkWarnChannel;
import com.youyu.framework.warn.platform.WechatWarnChannel;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

/**
 * 告警通道工厂类
 * <p>
 * 职责：根据配置自动创建对应平台的告警通道实例
 */
@Slf4j
public abstract class WarnChannelFactory {

	/**
	 * 创建告警通道实例
	 *
	 * @param properties       配置属性
	 * @param distinctManager  去重管理器
	 * @return 告警通道实例，如果配置无效返回null
	 */
	@Nullable
	public static MsgWarnChannel create(WarnProperties properties, WarnDistinctManager distinctManager) {
		// 验证必要配置
		if (properties.getUrl() == null || properties.getUrl().isEmpty()) {
			log.warn("===========>>>>>>> 告警Webhook URL未配置，告警功能将不可用");
			return null;
		}

		// 根据平台类型创建对应实现
		WarnPlatform platform = WarnPlatform.of(properties.getPlatform());
		if (platform == null) {
			log.warn("不支持的告警平台类型：{}，默认使用企业微信", properties.getPlatform());
			platform = WarnPlatform.WECHAT;
		}

		log.info("创建告警通道：平台={}, URL={}", platform.getName(), properties.getUrl());

		return switch (platform) {
			case WECHAT -> new WechatWarnChannel(properties, distinctManager);
			case DINGTALK -> new DingTalkWarnChannel(properties, distinctManager);
		};
	}

}
