package com.youyu.product.domain.service;

import java.util.Collections;
import java.util.List;

import com.youyu.common.constant.BaseI18nKey;
import com.youyu.framework.cache.redis.RedisKeyBuilder;
import com.youyu.framework.cache.redis.RedisUtil;
import com.youyu.framework.context.I18N;
import com.youyu.product.domain.aggregate.Product;
import com.youyu.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductDomainService {

    private final ProductRepository productRepository;

    private static final String DEDUCT_STOCK_LUA_SCRIPT = """
            local key = KEYS[1]
            local quantity = tonumber(ARGV[1])
            local current = tonumber(redis.call('GET', key) or '0')
            if current >= quantity then
                redis.call('DECRBY', key, quantity)
                return current - quantity
            else
                return -1
            end
            """;

    public void deductStock(Long productId, Integer quantity) {
        log.info("开始扣减库存，productId: {}, quantity: {}", productId, quantity);

        String stockKey = RedisKeyBuilder.Product.stock(productId);
        List<String> keys = Collections.singletonList(stockKey);
        List<String> args = Collections.singletonList(quantity.toString());

        Long remainingStock = RedisUtil.executeLuaScript(
                DEDUCT_STOCK_LUA_SCRIPT,
                keys,
                args,
                Long.class
        );

        if (remainingStock == null || remainingStock < 0) {
            log.warn("库存不足，productId: {}, quantity: {}", productId, quantity);
            throw new RuntimeException(I18N.msg(BaseI18nKey.PRODUCT_STOCK_INSUFFICIENT,
                (remainingStock != null ? remainingStock + quantity : "未知"), quantity));
        }

        log.info("库存扣减成功，productId: {}, remainingStock: {}", productId, remainingStock);
    }

    public void rollbackStock(Long productId, Integer quantity) {
        log.info("开始回滚库存，productId: {}, quantity: {}", productId, quantity);
        String stockKey = RedisKeyBuilder.Product.stock(productId);
        RedisUtil.opsForValue().increment(stockKey, quantity);
        log.info("库存回滚成功，productId: {}, quantity: {}", productId, quantity);
    }

    public Long getStock(Long productId) {
        String stockKey = RedisKeyBuilder.Product.stock(productId);
        String stock = RedisUtil.opsForValue().get(stockKey);
        return stock != null ? Long.parseLong(stock) : 0L;
    }

    public void initStock(Long productId, Long stock) {
        String stockKey = RedisKeyBuilder.Product.stock(productId);
        RedisUtil.opsForValue().set(stockKey, stock.toString());
        log.info("商品库存初始化成功，productId: {}, stock: {}", productId, stock);
    }

    public void deductStockFromDatabase(Long productId, Integer quantity) {
        log.info("开始数据库扣减库存，productId: {}, quantity: {}", productId, quantity);

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException(I18N.msg(BaseI18nKey.PRODUCT_NOT_FOUND)));

        if (!product.isStockSufficient(quantity)) {
            log.warn("库存不足，productId: {}, quantity: {}, currentStock: {}",
                productId, quantity, product.getStock());
            throw new RuntimeException(I18N.msg(BaseI18nKey.PRODUCT_STOCK_INSUFFICIENT,
                product.getStock(), quantity));
        }

        boolean success = productRepository.deductStock(productId, quantity);
        if (!success) {
            log.warn("数据库扣减失败（并发冲突），productId: {}, quantity: {}", productId, quantity);
            throw new RuntimeException(I18N.msg(BaseI18nKey.PRODUCT_STOCK_DEDUCT_FAILED));
        }

        log.info("数据库库存扣减成功，productId: {}, quantity: {}", productId, quantity);
    }
}