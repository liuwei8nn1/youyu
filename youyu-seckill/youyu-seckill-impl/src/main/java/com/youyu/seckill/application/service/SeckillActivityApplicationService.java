package com.youyu.seckill.application.service;

import com.youyu.common.model.Result;
import com.youyu.seckill.domain.aggregate.SeckillActivity;
import com.youyu.seckill.domain.repository.SeckillActivityRepository;
import com.youyu.seckill.domain.service.SeckillStockDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 秒杀活动应用服务（应用层）
 * <p>
 * 职责：
 * 1. 编排秒杀活动的业务流程
 * 2. 管理事务边界
 * 3. 协调领域服务和仓储
 */
@Slf4j
@Service
public class SeckillActivityApplicationService {

    @Resource
    private SeckillActivityRepository activityRepository;
    
    @Resource
    private SeckillStockDomainService stockDomainService;

    /**
     * 创建秒杀活动
     * <p>
     * 业务流程：
     * 1. 保存活动信息到数据库
     * 2. 同步到Redis缓存（包含库存初始化）
     * <p>
     * 注意：
     * - 两步操作在同一事务中，保证数据一致性
     *
     * @param activity 活动聚合根
     * @return 操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> createActivity(SeckillActivity activity) {
        try {
            // 1. 保存到数据库
            activityRepository.save(activity);
            
            // 2. 同步到 Redis（包含库存初始化）
            stockDomainService.cacheActivity(activity);
            
            log.info("秒杀活动创建成功，productId: {}", activity.getProductId());
            return Result.success();
        } catch (Exception e) {
            log.error("创建秒杀活动失败，productId: {}", activity != null ? activity.getProductId() : null, e);
            throw new RuntimeException("创建秒杀活动失败", e);
        }
    }

    /**
     * 更新秒杀活动
     * <p>
     * 业务流程：
     * 1. 更新数据库中的活动信息
     * 2. 同步更新Redis缓存（使用Pipeline批量操作）
     * <p>
     * 注意：
     * - 两步操作在同一事务中，保证数据一致性
     * - 如果Redis更新失败，整个事务回滚
     *
     * @param activity 活动聚合根
     * @return 操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateActivity(SeckillActivity activity) {
        try {
            // 参数校验
            if (activity == null || activity.getId() == null) {
                return Result.error("活动信息不完整");
            }

            // 1. 更新数据库
            activityRepository.update(activity);
            
            // 2. 同步到 Redis（使用 Pipeline 批量操作）
            stockDomainService.cacheActivity(activity);
            
            log.info("秒杀活动更新成功，id: {}", activity.getId());
            return Result.success();
        } catch (Exception e) {
            log.error("更新秒杀活动失败，id: {}", activity != null ? activity.getId() : null, e);
            throw new RuntimeException("更新秒杀活动失败", e);
        }
    }

    /**
     * 删除秒杀活动
     * <p>
     * 业务流程：
     * 1. 查询活动信息
     * 2. 删除Redis缓存（包括本地缓存）
     * 3. 删除数据库记录
     * <p>
     * 注意：
     * - 先删缓存再删数据库，避免脏数据
     * - 三步操作在同一事务中
     *
     * @param id 活动ID
     * @return 操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteActivity(Long id) {
        try {
            // 参数校验
            if (id == null || id <= 0) {
                return Result.error("活动ID无效");
            }

            // 1. 查询活动信息
            SeckillActivity activity = activityRepository.findById(id);
            
            if (activity != null) {
                // 2. 先删除 Redis 缓存（包括本地缓存）
                stockDomainService.removeCachedActivity(activity.getProductId());
            }
            
            // 3. 删除数据库记录
            activityRepository.delete(id);
            
            log.info("秒杀活动删除成功，id: {}", id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除秒杀活动失败，id: {}", id, e);
            throw new RuntimeException("删除秒杀活动失败", e);
        }
    }
}
