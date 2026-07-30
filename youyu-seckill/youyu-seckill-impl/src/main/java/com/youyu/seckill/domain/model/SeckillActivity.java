package com.youyu.seckill.domain.model;

import lombok.Getter;

import com.youyu.common.exception.DomainException;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀活动聚合根（领域层）
 * <p>
 * 职责：
 * 1. 封装秒杀活动的核心业务逻辑
 * 2. 保证秒杀活动数据的一致性
 * 3. 提供活动状态判断方法
 */
@Getter
public class SeckillActivity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 活动ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;

    /**
     * 活动结束时间
     */
    private LocalDateTime endTime;

    /**
     * 秒杀库存
     */
    private Integer stock;

    /**
     * 每人限购数量
     */
    private Integer limitPerUser;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    // ==================== 常量定义 ====================

    /**
     * 默认每人限购数量
     */
    private static final int DEFAULT_LIMIT_PER_USER = 1;

    /**
     * 无参构造函数（供 MapStruct 使用）
     */
    public SeckillActivity() {
    }

    private SeckillActivity(Long id, Long productId, LocalDateTime startTime,
                            LocalDateTime endTime, Integer stock, Integer limitPerUser,
                            LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.productId = productId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.stock = stock;
        this.limitPerUser = limitPerUser;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    /**
     * 创建秒杀活动
     *
     * @param productId   商品ID
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param stock       库存
     * @param limitPerUser 每人限购
     * @param seckillPrice 秒杀价格
     * @return 秒杀活动聚合根
     */
    public static SeckillActivity create(Long productId, LocalDateTime startTime,
                                                                        LocalDateTime endTime, Integer stock,
                                                                        Integer limitPerUser, BigDecimal seckillPrice) {
        SeckillActivity activity = new SeckillActivity();
        activity.productId = productId;
        activity.startTime = startTime;
        activity.endTime = endTime;
        activity.stock = stock;
        activity.limitPerUser = limitPerUser != null ? limitPerUser : DEFAULT_LIMIT_PER_USER;
        activity.seckillPrice = seckillPrice;
        activity.createTime = LocalDateTime.now();
        activity.updateTime = LocalDateTime.now();
        return activity;
    }

    /**
     * 从数据库恢复活动（用于查询）
     */
    public static SeckillActivity restore(Long id, Long productId, LocalDateTime startTime,
                                                                         LocalDateTime endTime, Integer stock,
                                                                         Integer limitPerUser, BigDecimal seckillPrice,
                                                                         LocalDateTime createTime,
                                                                         LocalDateTime updateTime) {
        SeckillActivity activity = new SeckillActivity();
        activity.id = id;
        activity.productId = productId;
        activity.startTime = startTime;
        activity.endTime = endTime;
        activity.stock = stock;
        activity.limitPerUser = limitPerUser;
        activity.seckillPrice = seckillPrice;
        activity.createTime = createTime;
        activity.updateTime = updateTime;
        return activity;
    }

    /**
     * 验证活动数据
     */
    public void validate() {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("商品ID无效");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("开始时间不能为空");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("结束时间不能为空");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }
        if (stock == null || stock <= 0) {
            throw new IllegalArgumentException("库存必须大于0");
        }
        if (limitPerUser == null || limitPerUser <= 0) {
            throw new IllegalArgumentException("每人限购数量必须大于0");
        }
        if (seckillPrice == null || seckillPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("秒杀价格必须大于0");
        }
    }

    /**
     * 判断活动是否正在进行中
     *
     * @return true-进行中，false-未开始或已结束
     */
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startTime) && !now.isAfter(endTime);
    }

    /**
     * 判断活动是否未开始
     */
    public boolean isNotStarted() {
        return LocalDateTime.now().isBefore(startTime);
    }

    /**
     * 判断活动是否已结束
     */
    public boolean isEnded() {
        return LocalDateTime.now().isAfter(endTime);
    }

    /**
     * 校验活动是否正在进行中
     *
     * @throws DomainException 活动未开始或已结束
     */
    public void assertActive() {
        if (isEnded()) {
            throw new DomainException("秒杀活动已结束");
        }
        if (isNotStarted()) {
            throw new DomainException("秒杀活动未开始");
        }
    }

    /**
     * 获取校验后的秒杀价格（保证不为空）
     *
     * @return 秒杀价格
     * @throws IllegalArgumentException 如果价格为 null
     */
    public BigDecimal getValidatedSeckillPrice() {
        if (seckillPrice == null) {
            throw new IllegalArgumentException("秒杀活动价格未配置，productId: " + productId);
        }
        return seckillPrice;
    }

    /**
     * 检查用户是否超过限购数量
     *
     * @param userPurchasedCount 用户已购买数量
     * @return true-可以购买，false-超过限购
     */
    public boolean canPurchase(Integer userPurchasedCount) {
        if (userPurchasedCount == null) {
            return true;
        }
        return userPurchasedCount < limitPerUser;
    }

    /**
     * 更新活动信息
     *
     * @param stock        新库存
     * @param limitPerUser 新限购数量
     */
    public void update(Integer stock, Integer limitPerUser) {
        if (stock != null && stock > 0) {
            this.stock = stock;
        }
        if (limitPerUser != null && limitPerUser > 0) {
            this.limitPerUser = limitPerUser;
        }
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 设置ID（供仓储层使用）
     *
     * @param id 活动ID
     */
    public void setId(Long id) {
        this.id = id;
    }
}
