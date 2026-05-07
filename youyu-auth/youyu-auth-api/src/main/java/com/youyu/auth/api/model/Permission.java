package com.youyu.auth.api.model;

import java.lang.annotation.*;

/**
 * 需要权限的注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission {
	/** 权限码：无需任何权限即可公开访问 */
	String NONE = "none";
	/** 权限码：登录用户(任意用户登录即可访问) */
	String LOGIN = "login";
	/** 权限码：外部顾客(顾客用户登录即可访问) */
	String CUSTOMER = "customer";
	/** 权限码：企业 + 平台(员工登录即可访问) */
	String EMP = "emp";
	/** 权限码：企业(企业员工登录即可访问) */
	String ENTERPRISE = "enterprise";
	/** 权限码：平台管理员 */
	String PLATFORM = "platform";

	/**
	 * 具体权限码
	 */
	String value() default "";

	/**
	 * 如配置了，则权限码检查通过后还会检查当前用户类型
	 */
	com.youyu.framework.context.UserType userType() default  com.youyu.framework.context.UserType.UNKNOWN ;

	/**
	 * 需要进行多权限管理的方法菜单数组<br>
	 * 该注解属性只能在方法上定义，否则无效
	 */
	Menu[] menus() default {};

	/** 是否为导出单独拆分权限 */
	boolean export() default false;

}
