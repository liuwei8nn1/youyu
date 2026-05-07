package com.youyu.starter.autoconfigure.data;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 数据层自动配置
 * <p>
 * 职责：
 * 1. MyBatis Plus 元数据处理器
 * 2. MyBatis Plus 分页插件
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor")
@Import({
		com.youyu.framework.datasource.mybatis.MybatisPlusMetaObjectHandler.class
})
public class DataAutoConfiguration {

	/**
	 * 配置 MyBatis-Plus 分页插件
	 * <p>
	 * 注意：必须配置此插件，否则分页查询不会执行 count 语句，
	 * 导致 Page.getTotal() 返回 0
	 */
	@Bean
	public MybatisPlusInterceptor mybatisPlusInterceptor() {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

		// 添加分页插件，指定数据库类型为 MySQL
		PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);

		// 设置最大单页限制数量，默认 500 条，-1 不受限制
		paginationInterceptor.setMaxLimit(500L);

		// 溢出总页数后是否进行处理（默认不处理）
		paginationInterceptor.setOverflow(false);

		interceptor.addInnerInterceptor(paginationInterceptor);

		return interceptor;
	}
}
