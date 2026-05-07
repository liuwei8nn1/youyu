package com.youyu.auth.api.model;

/**
 *
 * @since 2026/4/22
 */
public abstract class RedisKey {

	public static final String SPLIT = ":";

	/**
	 * 用户登录的设备是否被退出
	 * <p>
	 * key = user:login:${userType}:${identityId}:${deviceId}
	 * <p>
	 * value = exit
	 * <p>
	 * ttl: refreshTokenTtl * 2
	 */
	public static final String USER_LOGIN_DEVICEID = "user:login:";

	/**
	 * @see RedisKey#USER_LOGIN_DEVICEID
	 */
	public static String buildDeviceKey(Long identityId, Integer userType, String deviceId) {
		return RedisKey.USER_LOGIN_DEVICEID + userType + RedisKey.SPLIT +  identityId + RedisKey.SPLIT + deviceId;
	}

	/**
	 * 用户禁用
	 * <p>
	 * key = user:disabled:${userType}:${identityId}
	 * <p>
	 * value = "禁用原因|disabled"
	 */
	public static final String USER_DISABLE = "user:disabled:";
	/**
	 * @see RedisKey#USER_DISABLE
	 */
	public static String buildUserDisableKey(Long identityId, Integer userType) {
		return RedisKey.USER_DISABLE + userType + RedisKey.SPLIT +  identityId;
	}

	// ==================== 在线状态管理 ====================

	/**
	 * 用户在线状态（按用户维度，不区分设备）
	 * <p>
	 * key = user:presence:${userType}:${identityId}
	 * <p>
	 * value = 最后活跃时间戳（毫秒）
	 * <p>
	 * ttl = accessTokenTtl + 60s = 16分钟
	 * <p>
	 * 更新时机：刷新 Token 时（每15分钟）
	 */
	public static final String USER_PRESENCE = "user:presence:";

	/**
	 * @see RedisKey#USER_PRESENCE
	 */
	public static String buildPresenceKey(Long identityId, Integer userType) {
		return RedisKey.USER_PRESENCE + userType + RedisKey.SPLIT + identityId;
	}

}
