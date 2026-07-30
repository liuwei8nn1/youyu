package com.youyu.order.domain.model;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单聚合根（领域层）
 * <p>
 * 职责：
 * 1. 封装订单的核心业务逻辑
 * 2. 保证订单数据的一致性
 * 3. 提供订单状态流转方法
 */
@Getter
public class Order implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @Getter
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 订单类型：NORMAL-普通订单，SECKILL-秒杀订单
     */
    private String orderType;

    /**
     * 关联的秒杀活动ID（仅秒杀订单）
     */
    private Long activityId;

    /**
     * 支付过期时间
     */
    private LocalDateTime payExpireTime;

    /**
     * 订单状态：0-待支付, 1-已支付, 2-已发货, 3-已完成, 4-已取消
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 收货地址值对象（订单快照）
     */
    private ShippingAddress shippingAddress;

    // ==================== 常量定义 ====================

    /**
     * 订单类型：普通订单
     */
    public static final String ORDER_TYPE_NORMAL = "NORMAL";

    /**
     * 订单类型：秒杀订单
     */
    public static final String ORDER_TYPE_SECKILL = "SECKILL";

    /**
     * 订单状态：待支付
     */
    public static final int STATUS_PENDING = 0;

    /**
     * 订单状态：已支付
     */
    public static final int STATUS_PAID = 1;

    /**
     * 订单状态：已发货
     */
    public static final int STATUS_SHIPPED = 2;

    /**
     * 订单状态：已完成
     */
    public static final int STATUS_COMPLETED = 3;

    /**
     * 订单状态：已取消
     */
    public static final int STATUS_CANCELLED = 4;

    /**
     * 普通订单支付超时时间（分钟）
     */
    public static final int NORMAL_ORDER_PAY_TIMEOUT_MINUTES = 30;

    /**
     * 秒杀订单支付超时时间（分钟）
     */
    public static final int SECKILL_ORDER_PAY_TIMEOUT_MINUTES = 5;

    private Order() {
    }

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
    public static Order createNormalOrder(Long userId, Long productId, Integer quantity,
                                                                       BigDecimal price, ShippingAddress shippingAddress) {
        Order order = new Order();
        order.userId = userId;
        order.productId = productId;
        order.quantity = quantity;
        order.amount = price.multiply(new BigDecimal(quantity));
        order.orderType = ORDER_TYPE_NORMAL;
        order.status = STATUS_PENDING;
        order.shippingAddress = shippingAddress;
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
    public static Order createSeckillOrder(Long userId, Long productId, Integer quantity,
                                                                        BigDecimal amount, Long activityId,
                                                                        ShippingAddress shippingAddress) {
        Order order = new Order();
        order.userId = userId;
        order.productId = productId;
        order.quantity = quantity;
        order.amount = amount;
        order.orderType = ORDER_TYPE_SECKILL;
        order.activityId = activityId;
        order.status = STATUS_PENDING;
        order.shippingAddress = shippingAddress;
        return order;
    }

    /**
     * 从数据库恢复订单（用于查询）
     */
    public static Order restore(Long id, String orderNo, Long userId, Long productId,
                                                             Integer quantity, BigDecimal amount, String orderType,
                                                             Long activityId, LocalDateTime payExpireTime,
                                                             Integer status, LocalDateTime createTime, LocalDateTime updateTime) {
        Order order = new Order();
        order.id = id;
        order.orderNo = orderNo;
        order.userId = userId;
        order.productId = productId;
        order.quantity = quantity;
        order.amount = amount;
        order.orderType = orderType;
        order.activityId = activityId;
        order.payExpireTime = payExpireTime;
        order.status = status;
        order.createTime = createTime;
        order.updateTime = updateTime;
        return order;
    }

    /**
     * 初始化订单（设置订单号、ID、时间等）
     *
     * @param orderId       订单ID
     * @param orderNo       订单号
     * @param payExpireTime 支付过期时间
     */
    public void initialize(Long orderId, String orderNo, LocalDateTime payExpireTime) {
        this.id = orderId;
        this.orderNo = orderNo;
        this.payExpireTime = payExpireTime;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 验证订单
     */
    public void validate() {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID无效");
        }
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("商品ID无效");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("购买数量必须大于0");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("订单金额必须大于0");
        }
        if (orderType == null || (!ORDER_TYPE_NORMAL.equals(orderType) && !ORDER_TYPE_SECKILL.equals(orderType))) {
            throw new IllegalArgumentException("订单类型无效");
        }
        if (ORDER_TYPE_SECKILL.equals(orderType) && activityId == null) {
            throw new IllegalArgumentException("秒杀订单必须关联活动ID");
        }
    }

    /**
     * 判断是否为秒杀订单
     */
    public boolean isSeckillOrder() {
        return ORDER_TYPE_SECKILL.equals(orderType);
    }

    /**
     * 获取支付超时时间（分钟）
     */
    public int getPayTimeoutMinutes() {
        if (ORDER_TYPE_SECKILL.equals(orderType)) {
            return SECKILL_ORDER_PAY_TIMEOUT_MINUTES;
        }
        return NORMAL_ORDER_PAY_TIMEOUT_MINUTES;
    }

    /**
     * 判断订单是否已过期
     */
    public boolean isExpired() {
        if (payExpireTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(payExpireTime);
    }

    /**
     * 取消订单
     */
    public void cancel() {
        if (status != STATUS_PENDING) {
            throw new IllegalStateException("只有待支付订单才能取消");
        }
        this.status = STATUS_CANCELLED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 标记订单为已支付
     */
    public void markAsPaid() {
        if (status != STATUS_PENDING) {
            throw new IllegalStateException("只有待支付订单才能支付");
        }
        this.status = STATUS_PAID;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 标记订单为超时
     */
    public void markAsTimeout() {
        if (status != STATUS_PENDING) {
            throw new IllegalStateException("只有待支付订单才能标记为超时");
        }
        this.status = STATUS_CANCELLED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 判断订单是否已支付
     */
    public boolean isPaid() {
        return status == STATUS_PAID;
    }

    /**
     * 设置收货地址
     */
    public void setShippingAddress(ShippingAddress shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}