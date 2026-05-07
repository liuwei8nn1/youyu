package com.youyu.framework.datasource.mybatis;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;

public interface BaseRepository<T, K extends Serializable> {

	/**
	 * 根据主键ID获取对应的实体数据
	 */
	T get(K id);

	/**
	 * 获取任意一个实体数据
	 */
	T getAny();

	/**
	 * 根据 ID 查询对应的实体数据，并同时添加更新排它锁
	 */
	T getByIdForUpdate(K id);

	/**
	 * 根据主键ID数组获取对应的实体数据集合
	 */
	@SuppressWarnings("unchecked")
	List<T> findByIds(K... ids);

	/**
	 * 根据主键ID集合获取对应的实体数据集合
	 */
	List<T> findByIds(Collection<K> ids);

	/**
	 * 修改对应实体数据
	 */
	int update(T entity, Wrapper<T> updateWrapper);

	/**
	 * 根据主键ID数组修改对应的实体数据
	 */
	int update(T entity);

}
