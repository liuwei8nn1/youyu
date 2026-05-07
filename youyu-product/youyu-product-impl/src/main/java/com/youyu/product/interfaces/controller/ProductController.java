package com.youyu.product.interfaces.controller;

import java.math.BigDecimal;
import java.util.Locale;

import com.youyu.auth.api.model.Permission;
import com.youyu.framework.context.UserType;
import com.youyu.framework.context.UserInfo;
import com.youyu.framework.context.web.resolver.ProxyRequest;
import com.youyu.common.model.Result;
import com.youyu.product.api.dto.ProductDetailDTO;
import com.youyu.product.application.service.ProductApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductApplicationService productApplicationService;

    @PostMapping("/initStock")
    public Result<Void> initProductStock(@RequestParam Long productId, @RequestParam Long stock) {
        try {
            productApplicationService.initProductStock(productId, stock);
            return Result.success();
        } catch (Exception e) {
            log.error("初始化库存失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/stock/{productId}")
    @Permission(Permission.NONE)
    public Result<Long> getProductStock(ProxyRequest q, @PathVariable(name = "productId") Long productId) {
        try {
            UserInfo userInfo = q.getUserInfo();
            Locale locale = q.getLocale();
            Long stock = productApplicationService.getProductStock(productId);
            return Result.success(stock);
        } catch (Exception e) {
            log.error("查询库存失败", e);
            return Result.error(e.getMessage());
        }
    }

    @Permission(value = "product:create", userType = UserType.PLATFORM)
    @PostMapping("/create")
    public Result<Long> createProduct(@RequestParam("productName") String productName,
                                      @RequestParam(value = "description", required = false) String description,
                                      @RequestParam("price") BigDecimal price,
                                      @RequestParam("stock") Long stock) {
        try {
            Long productId = productApplicationService.createProduct(productName, description, price, stock);
            return Result.success(productId);
        } catch (Exception e) {
            log.error("创建商品失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/update/{productId}")
    public Result<Void> updateProduct(@PathVariable Long productId,
                                      @RequestParam(value = "productName", required = false) String productName,
                                      @RequestParam(value = "description", required = false) String description,
                                      @RequestParam(value = "price", required = false) BigDecimal price) {
        try {
            productApplicationService.updateProduct(productId, productName, description, price);
            return Result.success();
        } catch (Exception e) {
            log.error("更新商品失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/putOnShelf/{productId}")
    public Result<Void> putOnShelf(@PathVariable Long productId) {
        try {
            productApplicationService.putOnShelf(productId);
            return Result.success();
        } catch (Exception e) {
            log.error("上架商品失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/takeOffShelf/{productId}")
    public Result<Void> takeOffShelf(@PathVariable Long productId) {
        try {
            productApplicationService.takeOffShelf(productId);
            return Result.success();
        } catch (Exception e) {
            log.error("下架商品失败", e);
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{productId}")
    public Result<Void> deleteProduct(@PathVariable Long productId) {
        try {
            productApplicationService.deleteProduct(productId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除商品失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/category/create")
    public Result<Long> createCategory(@RequestParam(value = "categoryName") String categoryName,
                                       @RequestParam(value = "parentId", defaultValue = "0") Long parentId,
                                       @RequestParam(value = "level", required = false) Integer level,
                                       @RequestParam(value = "sortOrder", required = false) Integer sortOrder) {
        try {
            Long categoryId = productApplicationService.createCategory(categoryName, parentId, level, sortOrder);
            return Result.success(categoryId);
        } catch (Exception e) {
            log.error("创建分类失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/category/tree")
    public Result<?> getCategoryTree() {
        try {
            var tree = productApplicationService.getCategoryTree();
            return Result.success(tree);
        } catch (Exception e) {
            log.error("查询分类树失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/priceHistory/{productId}")
    public Result<?> getPriceHistory(@PathVariable Long productId,
                                     @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        try {
            var history = productApplicationService.getPriceHistory(productId, limit);
            return Result.success(history);
        } catch (Exception e) {
            log.error("查询价格历史失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/stockFlow/{productId}")
    public Result<?> getStockFlow(@PathVariable Long productId,
                                  @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        try {
            var flows = productApplicationService.getStockFlow(productId, limit);
            return Result.success(flows);
        } catch (Exception e) {
            log.error("查询库存流水失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据商品ID查询商品详情(供其他微服务调用)
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    @GetMapping("/detail/{productId}")
    public Result<ProductDetailDTO> getProductDetail(@PathVariable Long productId) {
        try {
            ProductDetailDTO productDetail = productApplicationService.getProductDetail(productId);
            return Result.success(productDetail);
        } catch (Exception e) {
            log.error("查询商品详情失败，productId: {}", productId, e);
            return Result.error(e.getMessage());
        }
    }
}