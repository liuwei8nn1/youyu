package com.youyu.product.interfaces.controller;

import com.youyu.common.model.Result;
import com.youyu.product.application.service.ProductApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/product_stock")
@RequiredArgsConstructor
public class StockController {

    private final ProductApplicationService productApplicationService;

    @PostMapping("/init")
    public Result<Void> initStock(@RequestParam Long productId, @RequestParam Long stock) {
        try {
            productApplicationService.initProductStock(productId, stock);
            return Result.success();
        } catch (Exception e) {
            log.error("初始化库存失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{productId}")
    public Result<Long> getStock(@PathVariable Long productId) {
        try {
            Long stock = productApplicationService.getProductStock(productId);
            return Result.success(stock);
        } catch (Exception e) {
            log.error("查询库存失败", e);
            return Result.error(e.getMessage());
        }
    }
}