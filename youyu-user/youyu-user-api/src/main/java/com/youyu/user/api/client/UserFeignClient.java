package com.youyu.user.api.client;

import com.youyu.common.model.Result;
import com.youyu.user.api.dto.AddressDTO;
import com.youyu.user.api.dto.CreateUserRequest;
import com.youyu.user.api.dto.UserLoginInfo;
import com.youyu.user.api.dto.UserProfileCreateRequest;
import com.youyu.user.api.dto.UserRoleInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户服务 Feign 客户端
 * <p>
 * 用于其他微服务调用 youyu-user 提供的用户查询接口
 * <p>
 * 工作模式:
 * - 如果配置了 service.url.youyu-user: 直接使用该 URL(K8s 环境,负载均衡由 K8s 处理)
 * - 如果未配置 service.url.youyu-user: 使用 name 从注册中心查找(传统微服务架构,客户端负载均衡)
 */
@FeignClient(name = "youyu-user", path = "/user", url = "${service.url.youyu-user:}")
public interface UserFeignClient {

    /**
     * 根据用户名查询用户登录信息
     * @param username 用户名
     * @param userType 用户类型：1-user, 2-merchant
     * @return 用户登录信息
     */
    @GetMapping("/by-username")
    Result<UserLoginInfo> getByUsername(@RequestParam(name = "username") String username,
                                        @RequestParam(name = "userType")  Integer userType);

    /**
     * 根据手机号查询用户登录信息
     * @param phone 手机号
     * @param userType 用户类型：1-user, 2-merchant
     * @return 用户登录信息
     */
    @GetMapping("/by-phone/{phone}")
    Result<UserLoginInfo> getByPhone(@PathVariable("phone") String phone,
                                     @RequestParam("userType") Integer userType);

    /**
     * 根据邮箱查询用户登录信息
     * @param email 邮箱
     * @param userType 用户类型：1-user, 2-merchant
     * @return 用户登录信息
     */
    @GetMapping("/by-email/{email}")
    Result<UserLoginInfo> getByEmail(@PathVariable("email") String email,
                                     @RequestParam("userType") Integer userType);

    /**
     * 查询用户的默认收货地址
     *
     * @param userId 用户ID
     * @return 默认收货地址
     */
    @GetMapping("/default-address/{userId}")
    Result<AddressDTO> getDefaultAddress(@PathVariable("userId") Long userId);

    /**
     * 获取用户的角色编码列表
     *
     * @param userId 用户ID
     * @return 用户角色信息
     */
    @GetMapping("/roles/{userId}")
    Result<UserRoleInfo> getUserRoles(@PathVariable("userId") Long userId);

    /**
     * 检查用户名是否存在
     */
    @PostMapping("/check-username")
    Result<Boolean> checkUsername(@RequestParam("username") String username,
                                  @RequestParam(value = "userType", required = false, defaultValue = "1") Integer userType);

    /**
     * 检查手机号是否存在
     */
    @PostMapping("/check-phone")
    Result<Boolean> checkPhone(@RequestParam("phone") String phone,
                               @RequestParam(value = "userType", required = false, defaultValue = "1") Integer userType);

    /**
     * 检查邮箱是否存在
     */
    @PostMapping("/check-email")
    Result<Boolean> checkEmail(@RequestParam("email") String email,
                               @RequestParam(value = "userType", required = false, defaultValue = "1") Integer userType);

    /**
     * 创建用户资料（供auth服务调用）
     */
    @PostMapping("/create-profile")
    Result<Long> createUserProfile(@RequestBody UserProfileCreateRequest request);

    /**
     * 创建用户（供auth服务调用）
     */
    @PostMapping("/create")
    Result<Long> createUser(@RequestBody CreateUserRequest request);

    /**
     * 分配角色给用户
     */
    @PostMapping("/assign-role")
    Result<Void> assignRoleToUser(@RequestParam("userId") Long userId,
                                  @RequestParam("roleId") Long roleId);

    /**
     * 获取用户的角色ID列表
     */
    @GetMapping("/role-ids/{userId}")
    Result<List<Long>> getUserRoleIds(@PathVariable("userId") Long userId);
}
