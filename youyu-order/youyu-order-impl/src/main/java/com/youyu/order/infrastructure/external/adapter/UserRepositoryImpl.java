package com.youyu.order.infrastructure.external.adapter;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.youyu.common.model.Result;
import com.youyu.order.domain.model.ShippingAddress;
import com.youyu.order.domain.repository.UserRepository;
import com.youyu.user.api.client.UserFeignClient;
import com.youyu.user.api.dto.AddressDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户仓储实现(基础设施层 - Adapter)
 * <p>
 * 【微服务稳定性治理实例 - 上游自我保护】
 * <p>
 * 职责:
 * 1. 实现领域层定义的 UserRepository 接口，隔离外部依赖。
 * 2. 封装对 UserFeignClient 的调用，并在本层处理熔断、限流与降级。
 * 3. DTO → Domain Object 转换(AddressDTO → ShippingAddress)。
 * <p>
 * 【关键配置说明】
 * 1. 超时控制 (Timeout):
 *    - 请在 application.yml 中通过 Feign 配置项设置。
 *    - 示例: feign.client.config.youyu-user.readTimeout=5000
 *    - 目的: 防止因远程服务响应过慢导致本系统线程池耗尽。
 * <p>
 * 2. 熔断与限流 (Circuit Breaking & Rate Limiting):
 *    - 使用 @SentinelResource 注解配合 Sentinel 控制台规则。
 *    - 资源名: out:user
 *    - 目的: 当远程服务异常比例过高或流量过大时，快速失败，保护 Order 服务核心业务。
 * <p>
 * 3. 降级策略 (Fallback):
 *    - 由本类中的 fallback4findDefaultAddress 方法定义。
 *    - 目的: 决定当外部服务不可用时，Order 业务是返回空值、使用缓存还是提示用户。
 * <p>
 * 【常见问题说明】
 * - Q: 同一个方法能加多个 @SentinelResource 吗？
 * - A: 不能。Java 注解不支持重复添加。如果需要多维度监控，建议在方法内部使用编程式 API (SphU.entry)
 *      进行资源嵌套，或者将复杂逻辑拆分为多个带注解的小方法。
 * - Q: 开启 feign.sentinel.enabled 后还需要这个注解吗？
 * - A: 建议保留。注解能提供更语义化的资源名和更精细的降级逻辑控制，而 Feign 自动集成更适合粗粒度的 URL 监控。
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserFeignClient userFeignClient;

    /**
     * 查询用户默认收货地址
     * <p>
     * 通过 Sentinel 保护该外部调用。如果触发限流或发生异常，将进入 fallback 逻辑。
     * 即使不配置 fallback，Sentinel 也会拦截请求并抛出 BlockException，防止系统因等待超时而挂起。
     */
    @SentinelResource(
            value = "out:user",
            blockHandler = "fallback", // 处理 Sentinel 限流/熔断
            fallback = "fallback"      // 处理业务异常（如超时、网络错误）
    )
    @Override
    public Optional<ShippingAddress> findDefaultAddress(Long userId) {
        log.info("查询用户默认收货地址，userId: {}", userId);

        // 1. 调用 Feign Client (超时时间由 yml 中的 connectTimeout/readTimeout 控制)
        Result<AddressDTO> result = userFeignClient.getDefaultAddress(userId);

        // 2. 检查结果
        if (result == null || !result.isSuccess() || result.getData() == null) {
            log.warn("未找到用户默认收货地址，userId: {}", userId);
            return Optional.empty();
        }

        AddressDTO addressDTO = result.getData();

        // 3. 转换为领域层的值对象
        ShippingAddress shippingAddress = ShippingAddress.create(
                addressDTO.getReceiverName(),
                addressDTO.getReceiverPhone(),
                addressDTO.getProvince(),
                addressDTO.getCity(),
                addressDTO.getDistrict(),
                addressDTO.getDetailAddress(),
                addressDTO.getZipCode()
        );

        log.info("查询用户默认收货地址成功，userId: {}, receiverName: {}",
                userId, addressDTO.getReceiverName());

        return Optional.of(shippingAddress);
    }

    /**
     * 降级处理方法 (Fallback / BlockHandler)
     * <p>
     * 当触发 Sentinel 规则或 Feign 调用异常时执行。
     * 这里的逻辑由 Order 服务自行决定，体现了“上游自我保护”的原则。
     *
     * @param userId 原始参数
     * @param e      触发的异常信息
     * @return 降级后的结果（此处选择返回空，让上层业务感知并提示用户）
     */
    public Optional<ShippingAddress> fallback(Long userId, Throwable e) {
        // 区分是 Sentinel 主动限流/熔断，还是底层发生的真实异常（如 SocketTimeout）
        if (e instanceof com.alibaba.csp.sentinel.slots.block.BlockException) {
            log.warn("查询用户地址被 Sentinel 限流/熔断，userId: {}", userId);
        } else {
            log.error("查询用户地址发生异常（可能是超时或网络故障），触发降级，userId: {}, error: {}", userId, e.getMessage());
        }

        // 业务决策：返回空 Optional，避免订单创建流程直接崩溃
        return Optional.empty();
    }

}