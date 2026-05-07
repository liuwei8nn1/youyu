package com.youyu.auth.api.model;

import java.lang.annotation.*;

/**
 * 菜单注解 - 用于从方法参数中提取动态权限码
 * <p>
 * 使用示例：
 * <pre>{@code
 * @Permission("product:view")
 * public Result<Void> updateProduct(
 *     @Menu(name = "status", args = {"type"}) String type,
 *     @RequestBody ProductDTO product
 * ) {
 *     // 最终权限码 = "product:view-" + type的值
 * }
 * }</pre>
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Menu {

	String DEFAULT_SUFFIX = "\0";

	/**
	 * 菜单名称
	 */
	String name();

	/**
	 * 菜单参数。例如：<code "1", " "1", "type", "1"} </code>
	 * 注意：* 表示任意非null, "" 表示null, 其他表示精确匹配
	 */
	String[] args() default {};

	/**
	 * 权限码后缀
	 * <pre>
	 * 存在多个menu，且没有配置suffix，则从第二个开始默认使用数组下标标识，固定通过 “-”分隔
	 *
	 * 数据库 permission_code 字段写入 -$suffix 区别
	 * </pre>
	 */
	String suffix() default DEFAULT_SUFFIX;

	/**
	 * 权限码
	 */
	String value() default "";

}