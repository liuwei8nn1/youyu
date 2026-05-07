package com.youyu.order.sdk;

import com.youyu.common.model.Result;
import com.youyu.order.api.client.OrderFeignClient;
import com.youyu.order.api.dto.SeckillOrderCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 订单服务 SDK 客户端
 * 提供便捷的远程调用封装
 * 
 * <p>使用示例:</p>
 * <pre>{@code
 * @Autowired
 * private OrderServiceClient orderServiceClient;
 * 
 * public void createSeckillOrder() {
 *     Long orderId = orderServiceClient.createSeckillOrder(
 *         userId, productId, quantity, amount, activityId
 *     );
 * }
 * }</pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderServiceClient {

    private final OrderFeignClient orderFeignClient;

    /**
     * 创建秒杀订单
     *
     * @param userId     用户ID
     * @param productId  商品ID
     * @param quantity   购买数量
     * @param amount     订单金额
     * @param activityId 秒杀活动ID
     * @return 订单ID
     */
    public Long createSeckillOrder(Long userId, Long productId, Integer quantity, 
                                    java.math.BigDecimal amount, Long activityId) {
        try {
            SeckillOrderCreateRequest request = new SeckillOrderCreateRequest(
                userId, productId, quantity, amount, activityId
            );
            
            Result<Long> result = orderFeignClient.createSeckillOrder(request);
            
            if (result != null && result.isSuccess()) {
                log.info("秒杀订单创建成功，orderId: {}", result.getData());
                return result.getData();
            } else {
                String errorMsg = result != null ? result.getMessage() : "未知错误";
                log.error("秒杀订单创建失败: {}", errorMsg);
                throw new RuntimeException("秒杀订单创建失败: " + errorMsg);
            }
            
        } catch (Exception e) {
            log.error("调用订单服务异常", e);
            throw new RuntimeException("调用订单服务异常", e);
        }
    }
}
