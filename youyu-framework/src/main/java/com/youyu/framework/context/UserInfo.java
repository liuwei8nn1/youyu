package com.youyu.framework.context;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

import com.youyu.common.util.StringUtil;
import lombok.*;

/**
 * 用户上下文信息
 * <p>
 * 从网关传递的请求头中提取的用户信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final UserInfo empty = new UserInfo();
    public static final List<String> emptyRoles = Collections.emptyList();

    public static final String JWT_USERINFO = "userInfo";
    public static final String JWT_SID = "sid";  // 用户身份ID (user_identity.id)
    public static final String JWT_USERID = "userId";
    public static final String JWT_USERTYPE = "userType";
    public static final String JWT_USERNAME = "userName";
    public static final String JWT_ROLES = "roles";
    public static final String JWT_DEVICEID = "deviceId";

    /**
     * 用户身份ID (session identity, user_identity.id)
     * <p>
     * Auth 领域的主键，用于授权相关查询
     */
    private Long sid;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户类型
     * @see com.youyu.framework.context.UserType
     */
    private Integer userType;

    /**
     * 角色列表（逗号分隔）
     */
    private String roles;

    private transient List<String> roleList;

    /**
     * 设备id
     */
    private Long deviceId;
    /**
     * 跟踪ID
     */
    private String traceId;

    public static UserInfo of(String traceId){
        UserInfo userInfo = new UserInfo();
        userInfo.setTraceId(traceId);
        return userInfo;
    }

    /**
     * 判断是否为空（userId 为 null 表示未登录）
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return userId == null;
    }

    /**
     * 判断是否已登录
     *
     * @return 是否已登录
     */
    public boolean isLogin() {
        return userId != null;
    }

    /**
     * 获取角色列表
     *
     * @return 角色列表，空列表表示无角色(放回的空列表是不可变的，不会被修改)
     */
    @NonNull
    public List<String> getRoleList() {
        if (roleList == null) {
            if(StringUtil.isEmpty(roles)){
                roleList = emptyRoles;
            }else {
	            roleList = StringUtil.splitAsStringList(roles);
            }
        }
        return roleList;
    }

    public boolean isPlatformAdmin() {
        return UserType.PLATFORM.getValue().equals(userType);
    }

    public boolean isEnterpriseEmployee() {
        return UserType.ENTERPRISE.getValue().equals(userType);
    }

    public boolean isCustomer() {
        return UserType.CUSTOMER.getValue().equals(userType);
    }

    public UserType getUserTypeEnum() {
        return UserType.of(userType);
    }


}
