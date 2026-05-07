package com.youyu.product.application.service;

import com.youyu.product.api.dto.ProductDetailDTO;
import com.youyu.product.domain.model.CategoryAggregate;
import com.youyu.product.domain.model.PriceHistory;
import com.youyu.product.domain.model.ProductAggregate;
import com.youyu.product.domain.model.StockFlow;
import com.youyu.product.domain.repository.CategoryRepository;
import com.youyu.product.domain.repository.PriceHistoryRepository;
import com.youyu.product.domain.repository.ProductRepository;
import com.youyu.product.domain.repository.StockFlowRepository;
import com.youyu.product.domain.service.ProductDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductApplicationService {

    private final ProductDomainService productDomainService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final StockFlowRepository stockFlowRepository;

    public ProductAggregate getProduct(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("商品不存在，productId: " + productId));
    }

    public boolean isSeckillProduct(Long productId) {
        ProductAggregate product = getProduct(productId);
        return product.isInSeckillPeriod();
    }

    public void initProductStock(Long productId, Long stock) {
        productDomainService.initStock(productId, stock);
    }

    public Long getProductStock(Long productId) {
        return productDomainService.getStock(productId);
    }

    @Transactional
    public Long createProduct(String productName, String description, BigDecimal price, Long stock) {
        log.info("开始创建商品，productName: {}", productName);

        ProductAggregate product = ProductAggregate.create(productName, description, price, stock);
        product.validate();
        productRepository.save(product);

        log.info("商品创建成功，productId: {}", product.getId());
        return product.getId();
    }

    @Transactional
    public void updateProduct(Long productId, String productName, String description, BigDecimal price) {
        log.info("开始更新商品，productId: {}", productId);

        ProductAggregate product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("商品不存在，productId: " + productId));

        if (price != null && !price.equals(product.getPrice())) {
            PriceHistory priceHistory = PriceHistory.create(
                productId, product.getPrice(), price, "商品更新", "system"
            );
            priceHistoryRepository.save(priceHistory);
        }

        product.updateInfo(productName, description, price);
        productRepository.update(product);

        log.info("商品更新成功，productId: {}", productId);
    }

    @Transactional
    public void putOnShelf(Long productId) {
        log.info("开始上架商品，productId: {}", productId);

        ProductAggregate product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("商品不存在，productId: " + productId));

        product.putOnShelf();
        productRepository.update(product);

        log.info("商品上架成功，productId: {}", productId);
    }

    @Transactional
    public void takeOffShelf(Long productId) {
        log.info("开始下架商品，productId: {}", productId);

        ProductAggregate product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("商品不存在，productId: " + productId));

        product.takeOffShelf();
        productRepository.update(product);

        log.info("商品下架成功，productId: {}", productId);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        log.info("开始删除商品，productId: {}", productId);

        ProductAggregate product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("商品不存在，productId: " + productId));

        product.markAsDeleted();
        productRepository.update(product);

        log.info("商品删除成功，productId: {}", productId);
    }

    @Transactional
    public Long createCategory(String categoryName, Long parentId, Integer level, Integer sortOrder) {
        log.info("开始创建商品分类，categoryName: {}, parentId: {}", categoryName, parentId);

        CategoryAggregate category;
        if (parentId == null || parentId == 0) {
            category = CategoryAggregate.createRoot(categoryName, sortOrder);
        } else {
            category = CategoryAggregate.createChild(categoryName, parentId, level, sortOrder);
        }

        category.validate();
        categoryRepository.save(category);

        log.info("商品分类创建成功，categoryId: {}", category.getId());
        return category.getId();
    }

    public List<CategoryAggregate> getCategoryTree() {
        return categoryRepository.findCategoryTree();
    }

    public List<PriceHistory> getPriceHistory(Long productId, Integer limit) {
        return priceHistoryRepository.findByProductId(productId, limit != null ? limit : 10);
    }

    public List<StockFlow> getStockFlow(Long productId, Integer limit) {
        return stockFlowRepository.findByProductId(productId, limit != null ? limit : 10);
    }

    /**
     * 查询商品详情(供其他微服务调用)
     *
     * @param productId 商品ID
     * @return 商品详情DTO
     */
    public ProductDetailDTO getProductDetail(Long productId) {
        log.info("查询商品详情，productId: {}", productId);
        
        ProductAggregate product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("商品不存在，productId: " + productId));
        
        // 检查商品是否已删除
        if (product.isDeleted()) {
            throw new RuntimeException("商品已删除，productId: " + productId);
        }
        
        // 检查商品是否上架
        if (!product.isOnShelf()) {
            throw new RuntimeException("商品未上架，productId: " + productId);
        }
        
        // 转换为 DTO
        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setProductId(product.getId());
        dto.setProductName(product.getProductName());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setStatus(product.getStatus());
        dto.setIsSeckill(product.getIsSeckill());
        
        log.info("商品详情查询成功，productId: {}, productName: {}", productId, product.getProductName());
        return dto;
    }
}