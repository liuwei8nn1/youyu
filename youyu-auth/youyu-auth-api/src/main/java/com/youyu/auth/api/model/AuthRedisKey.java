package com.youyu.auth.api.model;

/**
 * 认证业务 Redis Key
 *
 * @since 2026/4/22
 */
public interface AuthRedisKey {

	String SPLIT = ":";

	/**
	 * 用户登录的设备是否被退出
	 * <p>
	 * key = user:login:${userType}:${identityId}:${deviceId}
	 * <p>
	 * value = exit
	 * <p>
	 * ttl: refreshTokenTtl * 2
	 */
	String USER_LOGIN_DEVICEID = "user:login:";

	/**
	 * @see AuthRedisKey#USER_LOGIN_DEVICEID
	 */
	static String calcDeviceKey(Long identityId, Integer userType, String deviceId) {
		return AuthRedisKey.USER_LOGIN_DEVICEID + userType + AuthRedisKey.SPLIT + identityId + AuthRedisKey.SPLIT + deviceId;
	}

	/**
	 * 用户禁用
	 * <p>
	 * key = user:disabled:${userType}:${identityId}
	 * <p>
	 * value = "禁用原因|disabled"
	 */
	String USER_DISABLE = "user:disabled:";
	/**
	 * @see AuthRedisKey#USER_DISABLE
	 */
	static String calcUserDisableKey(Long identityId, Integer userType) {
		return AuthRedisKey.USER_DISABLE + userType + AuthRedisKey.SPLIT + identityId;
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
	String USER_PRESENCE = "user:presence:";

	/**
	 * @see AuthRedisKey#USER_PRESENCE
	 */
	static String calcPresenceKey(Long identityId, Integer userType) {
		return AuthRedisKey.USER_PRESENCE + userType + AuthRedisKey.SPLIT + identityId;
	}

}
