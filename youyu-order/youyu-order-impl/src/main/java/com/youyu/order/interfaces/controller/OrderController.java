package com.youyu.order.interfaces.controller;

import com.youyu.common.model.Result;
import com.youyu.order.application.service.OrderApplicationService;
import com.youyu.order.domain.aggregate.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器（接口层）
 * 负责处理所有订单相关的 HTTP 请求
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderApplicationService orderApplicationService;

    /**
     * 创建普通订单
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @param quantity  购买数量
     * @return 订单信息
     */
    @PostMapping("/create")
    public Result<Order> createOrder(@RequestParam Long userId,
                                     @RequestParam Long productId,
                                     @RequestParam Integer quantity) {
        log.info("收到普通订单创建请求，userId: {}, productId: {}, quantity: {}", 
            userId, productId, quantity);
        
        try {
            Order order = orderApplicationService.createOrder(userId, productId, quantity);
            log.info("普通订单创建成功，orderId: {}, orderNo: {}", order.getId(), order.getOrderNo());
            return Result.success(order);
        } catch (Exception e) {
            log.error("普通订单创建失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据订单ID查询订单
     *
     * @param orderId 订单ID
     * @return 订单信息
     */
    @GetMapping("/{orderId}")
    public Result<Order> getOrderById(@PathVariable Long orderId) {
        log.debug("查询订单，orderId: {}", orderId);
        
        try {
            Order order = orderApplicationService.getOrderById(orderId);
            return Result.success(order);
        } catch (Exception e) {
            log.error("查询订单失败，orderId: {}", orderId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单信息
     */
    @GetMapping("/no/{orderNo}")
    public Result<Order> getOrderByOrderNo(@PathVariable String orderNo) {
        log.debug("查询订单，orderNo: {}", orderNo);
        
        try {
            Order order = orderApplicationService.getOrderByOrderNo(orderNo);
            return Result.success(order);
        } catch (Exception e) {
            log.error("查询订单失败，orderNo: {}", orderNo, e);
            return Result.error(e.getMessage());
        }
    }
}
