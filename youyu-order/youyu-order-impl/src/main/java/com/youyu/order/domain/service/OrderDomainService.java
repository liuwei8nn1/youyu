package com.youyu.order.domain.service;

import com.youyu.order.domain.model.OrderAggregate;
import com.youyu.order.domain.model.ShippingAddress;
import com.youyu.common.util.CheckDigitUtil;
import com.youyu.common.model.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单领域服务（领域层）
 * <p>
 * 职责：
 * 1. 封装订单相关的业务逻辑
 * 2. 生成订单ID和订单号
 * 3. 验证订单数据的有效性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDomainService {

    private final SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 创建普通订单
     *
     * @param userId           用户ID
     * @param productId        商品ID
     * @param quantity         购买数量
     * @param price            商品单价
     * @param shippingAddress  收货地址快照
     * @return 订单聚合根
     */
    public OrderAggregate createNormalOrder(Long userId, Long productId, Integer quantity,
                                             BigDecimal price, ShippingAddress shippingAddress) {
        log.info("创建普通订单，userId: {}, productId: {}, quantity: {}", userId, productId, quantity);

        Long orderId = snowflakeIdGenerator.nextId();
        String orderNo = CheckDigitUtil.addCheckDigit(orderId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime payExpireTime = now.plusMinutes(OrderAggregate.NORMAL_ORDER_PAY_TIMEOUT_MINUTES);

        OrderAggregate order = OrderAggregate.createNormalOrder(userId, productId, quantity, price, shippingAddress);
        order.initialize(orderId, orderNo, payExpireTime);
        order.validate();

        log.info("普通订单创建成功，orderId: {}, orderNo: {}, amount: {}",
                orderId, orderNo, order.getAmount());
        return order;
    }

    /**
     * 创建秒杀订单
     *
     * @param userId           用户ID
     * @param productId        商品ID
     * @param quantity         购买数量
     * @param amount           订单金额
     * @param activityId       秒杀活动ID
     * @param shippingAddress  收货地址快照
     * @return 订单聚合根
     */
    public OrderAggregate createSeckillOrder(Long userId, Long productId, Integer quantity,
                                              BigDecimal amount, Long activityId, 
                                              ShippingAddress shippingAddress) {
        log.info("创建秒杀订单，userId: {}, productId: {}, activityId: {}", userId, productId, activityId);

        Long orderId = snowflakeIdGenerator.nextId();
        String orderNo = CheckDigitUtil.addCheckDigit(orderId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime payExpireTime = now.plusMinutes(OrderAggregate.SECKILL_ORDER_PAY_TIMEOUT_MINUTES);

        OrderAggregate order = OrderAggregate.createSeckillOrder(userId, productId, quantity, amount, activityId, shippingAddress);
        order.initialize(orderId, orderNo, payExpireTime);
        order.validate();

        log.info("秒杀订单创建成功，orderId: {}, orderNo: {}", orderId, orderNo);
        return order;
    }

    /**
     * 验证订单
     *
     * @param order 订单聚合根
     */
    public void validateOrder(OrderAggregate order) {
        if (order.getQuantity() <= 0) {
            throw new IllegalArgumentException("订单数量必须大于0");
        }

        if (order.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("订单金额不能为负数");
        }

        if (order.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        if (order.getProductId() == null) {
            throw new IllegalArgumentException("商品ID不能为空");
        }

        log.debug("订单验证通过，orderId: {}", order.getId());
    }

    /**
     * 处理订单超时（领域层）
     * <p>
     * 职责：
     * 1. 验证订单状态
     * 2. 标记订单为超时状态
     *
     * @param order 订单聚合根
     */
    public void handleOrderTimeout(OrderAggregate order) {
        log.info("处理订单超时，orderId: {}, orderType: {}", order.getId(), order.getOrderType());
        
        // 检查订单是否已支付
        if (order.isPaid()) {
            log.warn("订单已支付，无需处理超时，orderId: {}", order.getId());
            return;
        }
        
        // 标记订单为超时状态
        order.markAsTimeout();
        
        log.info("订单超时处理完成，orderId: {}", order.getId());
    }

    /**
     * 生成订单ID
     *
     * @return 订单ID
     */
    public String generateOrderId() {
        return String.valueOf(snowflakeIdGenerator.nextId());
    }
}