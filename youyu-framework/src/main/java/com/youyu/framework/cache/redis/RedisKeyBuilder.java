package com.youyu.framework.cache.redis;

/**
 * Redis Key 构建器（基础模块 - 通用基础设施）
 * <p>
 * 职责：
 * 1. 统一管理 Redis Key 的命名规范
 * 2. 提供类型安全的 Key 构建方法
 * 3. 避免硬编码字符串
 * <p>
 * 使用示例：
 * <pre>
 * String key = RedisKeyBuilder.Order.byId(123L);
 * // 结果: order:123
 * 
 * String key = RedisKeyBuilder.Product.stock(456L);
 * // 结果: product:stock:456
 * </pre>
 *
 * @since 2026/4/14
 */
public final class RedisKeyBuilder {

    public static final String SEPARATOR = ":";

    private RedisKeyBuilder() {
        // 工具类，禁止实例化
    }

    /**
     * 订单相关 Key
     */
    public static class Order {
        private static final String PREFIX = "order";

        /**
         * 订单详情 Key
         *
         * @param orderId 订单ID
         * @return order:{orderId}
         */
        public static String byId(Long orderId) {
            return PREFIX + SEPARATOR + orderId;
        }

        /**
         * 用户订单列表 Key
         *
         * @param userId 用户ID
         * @return order:user:{userId}
         */
        public static String byUserId(Long userId) {
            return PREFIX + SEPARATOR + "user" + SEPARATOR + userId;
        }

        /**
         * 订单号查询 Key
         *
         * @param orderNo 订单号
         * @return order:no:{orderNo}
         */
        public static String byOrderNo(String orderNo) {
            return PREFIX + SEPARATOR + "no" + SEPARATOR + orderNo;
        }
    }

    /**
     * 商品相关 Key
     */
    public static class Product {
        private static final String PREFIX = "product";

        /**
         * 商品详情 Key
         *
         * @param productId 商品ID
         * @return product:{productId}
         */
        public static String byId(Long productId) {
            return PREFIX + SEPARATOR + productId;
        }

        /**
         * 商品库存 Key（用于防超卖）
         *
         * @param productId 商品ID
         * @return product:stock:{productId}
         */
        public static String stock(Long productId) {
            return PREFIX + SEPARATOR + "stock" + SEPARATOR + productId;
        }

        /**
         * 商品销量 Key
         *
         * @param productId 商品ID
         * @return product:sales:{productId}
         */
        public static String sales(Long productId) {
            return PREFIX + SEPARATOR + "sales" + SEPARATOR + productId;
        }
    }

    /**
     * 用户相关 Key
     */
    public static class User {
        private static final String PREFIX = "user";

        /**
         * 用户信息 Key
         *
         * @param userId 用户ID
         * @return user:{userId}
         */
        public static String byId(Long userId) {
            return PREFIX + SEPARATOR + userId;
        }

        /**
         * 用户会话 Key
         *
         * @param userId 用户ID
         * @return user:session:{userId}
         */
        public static String session(Long userId) {
            return PREFIX + SEPARATOR + "session" + SEPARATOR + userId;
        }
    }

    /**
     * 分布式锁 Key
     */
    public static class Lock {
        private static final String PREFIX = "lock";

        /**
         * 订单处理锁
         *
         * @param orderId 订单ID
         * @return lock:order:{orderId}
         */
        public static String order(Long orderId) {
            return PREFIX + SEPARATOR + "order" + SEPARATOR + orderId;
        }

        /**
         * 库存扣减锁
         *
         * @param productId 商品ID
         * @return lock:stock:{productId}
         */
        public static String stock(Long productId) {
            return PREFIX + SEPARATOR + "stock" + SEPARATOR + productId;
        }

        /**
         * 用户操作锁（防止重复提交）
         *
         * @param userId 用户ID
         * @return lock:user:{userId}
         */
        public static String user(Long userId) {
            return PREFIX + SEPARATOR + "user" + SEPARATOR + userId;
        }
    }

    /**
     * 限流 Key
     */
    public static class RateLimit {
        private static final String PREFIX = "ratelimit";

        /**
         * 接口限流 Key
         *
         * @param api 接口路径
         * @param userId 用户ID
         * @return ratelimit:{api}:{userId}
         */
        public static String byApiAndUser(String api, Long userId) {
            return PREFIX + SEPARATOR + api + SEPARATOR + userId;
        }

        /**
         * IP 限流 Key
         *
         * @param ip IP地址
         * @return ratelimit:ip:{ip}
         */
        public static String byIp(String ip) {
            return PREFIX + SEPARATOR + "ip" + SEPARATOR + ip;
        }
    }

    /**
     * 秒杀相关 Key
     */
    public static class Seckill {
        private static final String PREFIX = "seckill";

        /**
         * 秒杀库存 Key（用于防超卖）
         *
         * @param productId 商品ID
         * @return seckill:stock:{productId}
         */
        public static String stock(Long productId) {
            return PREFIX + SEPARATOR + "stock" + SEPARATOR + productId;
        }

        /**
         * 秒杀价格 Key（缓存秒杀价）
         *
         * @param productId 商品ID
         * @return seckill:price:{productId}
         */
        public static String price(Long productId) {
            return PREFIX + SEPARATOR + "price" + SEPARATOR + productId;
        }

        /**
         * 用户购买限制 Key
         *
         * @param userId    用户ID
         * @param productId 商品ID
         * @return seckill:user:{userId}:{productId}
         */
        public static String userLimit(Long userId, Long productId) {
            return PREFIX + SEPARATOR + "user" + SEPARATOR + userId + SEPARATOR + productId;
        }

        /**
         * 秒杀活动详情 Key
         *
         * @param activityId 活动ID
         * @return seckill:activity:{activityId}
         */
        public static String activity(Long activityId) {
            return PREFIX + SEPARATOR + "activity" + SEPARATOR + activityId;
        }

        /**
         * 用户操作频率限制 Key（防止重复点击）
         *
         * @param userId    用户ID
         * @param productId 商品ID
         * @return seckill:freq:{userId}:{productId}
         */
        public static String userFrequencyLimit(Long userId, Long productId) {
            return PREFIX + SEPARATOR + "freq" + SEPARATOR + userId + SEPARATOR + productId;
        }
    }

    /**
     * 自定义 Key 构建
     *
     * @param parts Key 片段
     * @return 拼接后的 Key
     */
    public static String custom(String... parts) {
        return String.join(SEPARATOR, parts);
    }
}
