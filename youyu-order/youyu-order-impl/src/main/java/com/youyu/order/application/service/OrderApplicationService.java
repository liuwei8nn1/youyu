package com.youyu.order.application.service;

import com.youyu.common.exception.DomainException;
import com.youyu.common.util.CheckDigitUtil;
import com.youyu.framework.context.I18N;
import com.youyu.order.api.dto.SeckillOrderTimeoutMessage;
import com.youyu.order.domain.model.OrderAggregate;
import com.youyu.order.domain.model.ShippingAddress;
import com.youyu.order.domain.repository.OrderRepository;
import com.youyu.order.domain.repository.ProductRepository;
import com.youyu.order.domain.repository.UserRepository;
import com.youyu.order.domain.service.OrderDomainService;
import com.youyu.order.infrastructure.messaging.NormalStockRollbackMessageProducer;
import com.youyu.order.infrastructure.messaging.OrderTimeoutMessageProducer;
import com.youyu.order.infrastructure.messaging.SeckillStockRollbackMessageProducer;
import com.youyu.product.api.dto.ProductDetailDTO;

import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final OrderDomainService orderDomainService;
    private final OrderRepository orderRepository;
    private final OrderTimeoutMessageProducer orderTimeoutMessageProducer;
    private final NormalStockRollbackMessageProducer normalStockRollbackMessageProducer;
    private final SeckillStockRollbackMessageProducer seckillStockRollbackMessageProducer;
    private final UserRepository userRepository;      // ← 依赖领域层接口
    private final ProductRepository productRepository; // ← 依赖领域层接口

    /**
     * 创建普通订单（应用层编排）
     * <p>
     * 职责：
     * 1. 查询商品详情(HTTP同步调用 product-service)
     * 2. 查询用户收货地址(HTTP同步调用 user-service)
     * 3. 调用领域服务创建订单
     * 4. 保存订单到数据库
     * 5. 发送订单超时延时消息
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @param quantity  购买数量
     * @return 订单聚合根
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderAggregate createOrder(Long userId, Long productId, Integer quantity) {
        log.info("普通订单创建开始，userId: {}, productId: {}, quantity: {}", userId, productId, quantity);

        // 1. 查询商品详情
        ProductDetailDTO product = productRepository.findById(productId)
            .orElseThrow(() -> new DomainException("商品不存在"));

        // 2. 查询用户默认收货地址
        ShippingAddress shippingAddress = userRepository.findDefaultAddress(userId)
            .orElseThrow(() -> new DomainException("未找到默认收货地址"));

        // 3. 调用领域服务创建订单（内部完成金额计算）
        OrderAggregate order = orderDomainService.createNormalOrder(
            userId, productId, quantity, product.getPrice(), shippingAddress
        );

        // 4. 保存订单到数据库
        orderRepository.save(order);

        // 5. 发送订单超时延时消息
        orderTimeoutMessageProducer.sendOrderTimeoutMessage(
            order.getId(),
            userId,
            productId,
            quantity,
            "NORMAL",
            null
        );

        log.info("普通订单创建成功，orderId: {}, orderNo: {}, amount: {}", 
            order.getId(), order.getOrderNo(), order.getAmount());
        return order;
    }

    /**
     * 创建秒杀订单（供 MQ 消费者调用）
     * <p>
     * 职责：
     * 1. 查询用户收货地址(HTTP同步调用 user-service)
     * 2. 调用领域服务创建订单(价格已从 MQ 消息中获取)
     * 3. 保存订单到数据库
     * 4. 发送订单超时延时消息
     *
     * @param userId      用户ID
     * @param productId   商品ID
     * @param quantity    购买数量
     * @param amount      订单金额(从 MQ 消息中获取)
     * @param activityId  秒杀活动ID
     * @return 订单聚合根
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderAggregate createSeckillOrder(Long userId, Long productId, Integer quantity, 
                                              BigDecimal amount, Long activityId) {
        log.info("秒杀订单创建开始，userId: {}, productId: {}, activityId: {}", userId, productId, activityId);

        // 1. 查询用户默认收货地址(通过仓储接口)
        ShippingAddress shippingAddress = userRepository.findDefaultAddress(userId)
            .orElseThrow(() -> new RuntimeException("未找到默认收货地址，userId: " + userId));

        // 2. 调用领域服务创建订单(价格已从 MQ 消息中获取，无需再查)
        OrderAggregate order = orderDomainService.createSeckillOrder(
            userId, productId, quantity, amount, activityId, shippingAddress
        );

        // 3. 保存订单到数据库
        orderRepository.save(order);

        // 4. 发送订单超时延时消息（5分钟后检查是否支付）
        orderTimeoutMessageProducer.sendOrderTimeoutMessage(
            order.getId(),
            userId,
            productId,
            quantity,
            "SECKILL",
            activityId
        );

        log.info("秒杀订单创建成功，orderId: {}, orderNo: {}", order.getId(), order.getOrderNo());
        return order;
    }

    /**
     * 检查订单是否已支付
     *
     * @param orderId 订单ID
     * @return 是否已支付
     */
    public boolean isOrderPaid(Long orderId) {
        OrderAggregate order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }
        return order.isPaid();
    }

    /**
     * 更新订单状态为超时
     *
     * @param orderId 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatusToTimeout(Long orderId) {
        OrderAggregate order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.markAsTimeout();
            orderRepository.update(order);
            log.info("订单状态更新为超时，orderId: {}", orderId);
        }
    }

    /**
     * 处理订单超时（应用层编排）
     * <p>
     * 职责：
     * 1. 查询订单
     * 2. 调用领域服务处理订单超时状态
     * 3. 更新订单到数据库
     * 4. 根据订单类型回滚库存：
     *    - 普通订单：直接操作数据库回滚库存
     *    - 秒杀订单：发送 MQ 消息给 seckill-service 回滚 Redis 库存
     *
     * @param timeoutMessage 超时消息
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderTimeout(SeckillOrderTimeoutMessage timeoutMessage) {
        log.info("开始处理订单超时，orderId: {}, orderType: {}", 
            timeoutMessage.getOrderId(), timeoutMessage.getOrderType());
        
        // 1. 查询订单
        OrderAggregate order = orderRepository.findById(timeoutMessage.getOrderId())
            .orElseThrow(() -> new RuntimeException("订单不存在，orderId: " + timeoutMessage.getOrderId()));
        
        // 2. 调用领域服务处理订单超时（检查支付状态、标记超时）
        orderDomainService.handleOrderTimeout(order);
        
        // 3. 更新订单到数据库
        orderRepository.update(order);
        
        // 4. 根据订单类型回滚库存（通过 MQ 异步通知商品服务）
        if ("NORMAL".equals(timeoutMessage.getOrderType())) {
            // 普通订单：发送 MQ 消息给 product-service 回滚数据库库存
            rollbackNormalOrderStock(
                timeoutMessage.getOrderId(),
                timeoutMessage.getProductId(),
                timeoutMessage.getQuantity()
            );
        } else if ("SECKILL".equals(timeoutMessage.getOrderType())) {
            // 秒杀订单：发送 MQ 消息给 seckill-service 回滚 Redis 库存
            rollbackSeckillOrderStock(
                timeoutMessage.getOrderId(),
                timeoutMessage.getUserId(),
                timeoutMessage.getProductId(),
                timeoutMessage.getQuantity(),
                timeoutMessage.getActivityId()
            );
        }
        
        log.info("订单超时处理完成，orderId: {}, orderType: {}", 
            timeoutMessage.getOrderId(), timeoutMessage.getOrderType());
    }

    /**
     * 回滚普通订单库存（发送 MQ 消息）
     *
     * @param orderId   订单ID
     * @param productId 商品ID
     * @param quantity  回滚数量
     */
    private void rollbackNormalOrderStock(Long orderId, Long productId, Integer quantity) {
        log.info("发送普通订单库存回滚消息，orderId: {}, productId: {}, quantity: {}", 
            orderId, productId, quantity);
        normalStockRollbackMessageProducer.sendNormalStockRollbackMessage(orderId, productId, quantity);
        log.info("普通订单库存回滚消息发送成功，orderId: {}, productId: {}", orderId, productId);
    }

    /**
     * 回滚秒杀订单库存（发送 MQ 消息）
     *
     * @param orderId    订单ID
     * @param userId     用户ID
     * @param productId  商品ID
     * @param quantity   回滚数量
     * @param activityId 活动ID
     */
    private void rollbackSeckillOrderStock(Long orderId, Long userId, Long productId, 
                                            Integer quantity, Long activityId) {
        log.info("发送秒杀订单库存回滚消息，orderId: {}, activityId: {}", orderId, activityId);
        seckillStockRollbackMessageProducer.sendSeckillStockRollbackMessage(
            orderId, userId, productId, quantity, activityId
        );
        log.info("秒杀订单库存回滚消息发送成功，orderId: {}, activityId: {}", orderId, activityId);
    }

    public OrderAggregate getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("订单不存在，orderId: " + orderId));
    }

    public OrderAggregate getOrderByOrderNo(String orderNo) {
        I18N.assertTrue(CheckDigitUtil.validate(orderNo));
        return orderRepository.findByOrderNo(orderNo)
            .orElseThrow(() -> new RuntimeException("订单不存在，orderNo: " + orderNo));
    }
}