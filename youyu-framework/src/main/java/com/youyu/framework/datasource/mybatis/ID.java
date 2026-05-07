package com.youyu.framework.datasource.mybatis;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

public interface ID extends Serializable {

	Serializable getId();

	/**
	 * 获取用于进行关联记录的对象类型名称
	 */
	default String entityName() {
		return Bean.entityName(entityType());
	}

	default Class<?> entityType() {
		return Bean.getClass(this);
	}

	/**
	 * 指示当前实体是否具有有效的 id
	 */
	default boolean hasValidId() {
		return getId() != null;
	}

	/**
	 * 指示指定的对象是否存在有效的 ID 值
	 */
	static boolean hasId(@Nullable ID obj) {
		return obj != null && obj.hasValidId();
	}

}