package com.youyu.framework.datasource.mybatis;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.exceptions.TooManyResultsException;

/**
 * 数据访问层的公共抽象父接口，用于封装通用的数据访问工具方法<br>
 */
public interface BaseDao<T> extends BaseMapper<T> {

	/**
	 * 获取 wrapper 自定义 WHERE 子句 的表达式。
	 * 入参必须有：@Param("ew") Wrapper<?> wrapper
	 */
	String CUSTOM_WHERE_SQL = "${ew.customSqlSegment}";

	public static final Object uninitialized = new Object();
	@SuppressWarnings("unchecked")
	@Override
	default T selectOne(Wrapper<T> queryWrapper, boolean throwEx) {
		final Object empty = uninitialized;
		final Object[] ref = { empty };
		this.selectList(queryWrapper, ctx -> {
			if (ref[0] == empty) {
				ref[0] = ctx.getResultObject();
			} else {
				if (throwEx) {
					throw new TooManyResultsException("Expected one result (or null) to be returned by selectOne(), but found multiple records");
				}
				ctx.stop();
			}
		});
		return ref[0] == empty ? null : (T) ref[0];
	}

}
