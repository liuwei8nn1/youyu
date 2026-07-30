package com.youyu.seckill.interfaces.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.youyu.common.model.Result;
import com.youyu.seckill.application.dto.SeckillOrderResponse;
import com.youyu.seckill.application.service.SeckillActivityApplicationService;
import com.youyu.seckill.application.service.SeckillOrderApplicationService;
import com.youyu.seckill.interfaces.vo.SeckillActivityRequest;
import com.youyu.seckill.interfaces.vo.SeckillActivityVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 秒杀控制器（接口层）
 */
@Slf4j
@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillActivityApplicationService activityApplicationService;
    private final SeckillOrderApplicationService seckillOrderService;

    /**
     * 查询所有秒杀活动
     */
    @GetMapping("/list")
    public Result<List<SeckillActivityVO>> listAll() {
        try {
            List<SeckillActivityVO> activities = activityApplicationService.listAll();
            return Result.success(activities);
        } catch (Exception e) {
            log.error("查询秒杀活动列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询秒杀活动详情
     */
    @GetMapping("/detail/{id}")
    public Result<SeckillActivityVO> getById(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return Result.error("活动ID无效");
            }
            SeckillActivityVO activity = activityApplicationService.getById(id);
            if (activity == null) {
                return Result.error("活动不存在");
            }
            return Result.success(activity);
        } catch (Exception e) {
            log.error("查询秒杀活动详情失败，id: {}", id, e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 创建秒杀活动
     */
    @PostMapping("/save")
    public Result<Void> save(@RequestBody SeckillActivityRequest request) {
        try {
            if (request == null) {
                return Result.error("活动信息不能为空");
            }
            return activityApplicationService.createActivity(request);
        } catch (Exception e) {
            log.error("创建秒杀活动失败", e);
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    /**
     * 更新秒杀活动
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody SeckillActivityRequest request) {
        try {
            return activityApplicationService.updateActivity(request);
        } catch (Exception e) {
            log.error("更新秒杀活动失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除秒杀活动
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            // 参数校验
            if (id == null || id <= 0) {
                return Result.error("活动ID无效");
            }
            
            // 调用应用服务（包含缓存删除和数据库删除）
            return activityApplicationService.deleteActivity(id);
        } catch (Exception e) {
            log.error("删除秒杀活动失败，id: {}", id, e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 发起秒杀
     *
     * @param productId 商品ID
     * @param userId    用户ID（从请求头获取）
     * @return 秒杀结果
     */
    @PostMapping("/{productId}")
    @SentinelResource(value = "seckill", blockHandler = "seckillBlockHandler")
    public Result<SeckillOrderResponse> seckill(@PathVariable Long productId,
                                                @RequestHeader("X-User-Id") String userId,
                                                @RequestParam(value = "quantity", defaultValue = "1") Integer quantity) {
        try {
            // 参数校验
            if (productId == null || productId <= 0) {
                return Result.error("商品ID无效");
            }
            if (userId == null || userId.isEmpty()) {
                return Result.error("用户ID不能为空");
            }
            if (quantity == null || quantity <= 0) {
                return Result.error("购买数量必须大于0");
            }

            // 调用应用服务，直接返回Result
            return seckillOrderService.createSeckillOrder(
                    Long.parseLong(userId), productId, quantity);
        } catch (NumberFormatException e) {
            log.warn("用户ID格式错误，userId: {}", userId, e);
            return Result.error("用户ID格式错误");
        } catch (Exception e) {
            log.error("秒杀处理异常，productId: {}, userId: {}", productId, userId, e);
            return Result.error("秒杀失败：" + e.getMessage());
        }
    }

    /**
     * 秒杀接口限流处理
     */
    public Result<SeckillOrderResponse> seckillBlockHandler(Long productId, String userId, 
                                                             Integer quantity, BlockException ex) {
        log.warn("秒杀接口被限流，productId: {}, userId: {}", productId, userId);
        return Result.error("系统繁忙，请稍后重试");
    }
}
