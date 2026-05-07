package com.youyu.common.constant;

/**
 * 基础国际化键值常量类
 * 用于统一管理国际化消息的key，便于复用和维护
 */
public class BaseI18nKey {

    // ==================== Message 相关 ====================
    /** 操作成功 */
    public static final String MESSAGE_SUCCESS = "message.success";
    
    /** 操作失败 */
    public static final String MESSAGE_ERROR = "message.error";

    // ==================== 通用错误消息 ====================
    /** 资源未找到 */
    public static final String COMMON_NOT_FOUND = "common.not.found";
    
    /** 参数无效 */
    public static final String COMMON_INVALID_PARAM = "common.invalid.param";
    
    /** 服务器内部错误 */
    public static final String COMMON_SERVER_ERROR = "common.server.error";

    // ==================== Sentinel 限流降级相关 ====================
    /** 请求被限流 */
    public static final String SENTINEL_FLOW_CONTROL = "sentinel.flow.control";
    
    /** 服务已降级 */
    public static final String SENTINEL_DEGRADE = "sentinel.degrade";
    
    /** 热点参数限流 */
    public static final String SENTINEL_PARAM_FLOW = "sentinel.param.flow";
    
    /** 系统保护触发 */
    public static final String SENTINEL_SYSTEM_BLOCK = "sentinel.system.block";
    
    /** 授权规则不通过 */
    public static final String SENTINEL_AUTHORITY_DENIED = "sentinel.authority.denied";

    // ==================== 业务异常相关（可扩展）====================
    /** 业务异常 */
    public static final String BUSINESS_EXCEPTION = "business.exception";
    
    /** 数据验证失败 */
    public static final String VALIDATION_FAILED = "validation.failed";
    
    /** 权限不足 */
    public static final String PERMISSION_DENIED = "permission.denied";
    
    /** 未登录 */
    public static final String NOT_LOGGED_IN = "not.logged.in";

    // ==================== 商品相关 ====================
    /** 商品不存在 */
    public static final String PRODUCT_NOT_FOUND = "product.not.found";
    
    /** 库存不足 */
    public static final String PRODUCT_STOCK_INSUFFICIENT = "product.stock.insufficient";
    
    /** 库存扣减失败（并发冲突） */
    public static final String PRODUCT_STOCK_DEDUCT_FAILED = "product.stock.deduct.failed";

    // ==================== 订单相关 ====================
    /** 订单创建失败 */
    public static final String ORDER_CREATE_FAILED = "order.create.failed";
    
    /** 订单不存在 */
    public static final String ORDER_NOT_FOUND = "order.not.found";
    
    /** 请勿重复提交 */
    public static final String ORDER_DUPLICATE_SUBMIT = "order.duplicate.submit";
    
    /** 操作过于频繁，请稍后重试 */
    public static final String ANTI_SHAKE_LIMIT = "anti.shake.limit";
    public static final String AUTH_TOKEN_INVALID = "auth.token.invalid";
    
    /** Token 缺失（未登录） */
    public static final String AUTH_TOKEN_REQUIRED = "auth.token.required";
    
    /** 权限不足 */
    public static final String AUTH_PERMISSION_DENIED = "auth.permission.denied";

}
