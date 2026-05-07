package com.youyu.auth.api;

import java.util.List;

import com.youyu.auth.api.model.RedisKey;
import com.youyu.common.util.StringUtil;
import com.youyu.framework.context.UserInfo;
import org.jspecify.annotations.*;

/**
 *
 * @author LiuWei
 * @since 2026/4/22
 */
public class GateWayCheckUser {

	@NullMarked
	public static List<String> getCheckRedisKeys(UserInfo userInfo){
		return List.of(
				RedisKey.buildUserDisableKey(userInfo.getSid(),userInfo.getUserType()),
				RedisKey.buildDeviceKey(userInfo.getSid(),userInfo.getUserType(),String.valueOf(userInfo.getDeviceId()))
				);
	}


	@Nullable
	public static String check4RedisValues(@NonNull List<String> redisValues){
		String disabledValue = redisValues.get(0);
		String loginValue = redisValues.get(1);
		// 检查用户是否被禁用
		if (!StringUtil.isEmpty(disabledValue)) {
			return "auth.user.disabled";
		}

		// 检查设备是否在线
		if (!StringUtil.isEmpty(loginValue)) {
			return "auth.user.disabled";
		}
		return null;
	}

}
