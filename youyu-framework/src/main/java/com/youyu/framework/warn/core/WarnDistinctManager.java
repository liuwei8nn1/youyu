package com.youyu.framework.warn.core;

/**
 * 告警去重管理器接口
 * <p>
 * 职责：防止相同告警消息在短时间内重复发送
 */
public interface WarnDistinctManager {

	String CACHE_NAME = "ALARM_";

	/**
	 * 是否可以发送
	 *
	 * @param msgKey 唯一标识消息内容的 key（通常是MD5）
	 * @return true=可以发送，false=已发送过需跳过
	 */
	boolean canSend(String msgKey);

}
