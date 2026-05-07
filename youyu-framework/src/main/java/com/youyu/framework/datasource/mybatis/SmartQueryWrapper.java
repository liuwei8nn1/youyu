package com.youyu.framework.datasource.mybatis;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlInjectionUtils;
import com.youyu.common.util.StringUtil;
import org.jspecify.annotations.Nullable;

/**
 * 内部自动基于参数值进行过滤处理 的 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper} 增强版
 */
public class SmartQueryWrapper<T> extends AbstractWrapper<T, String, SmartQueryWrapper<T>> implements Query<SmartQueryWrapper<T>, T, String> {

	/**
	 * 查询字段
	 */
	protected final SharedString sqlSelect = new SharedString();

	public static final Predicate<Object> NOT_EMPTY = o -> o != null && (!(o instanceof String str) || !str.isEmpty());
	public static final Predicate<Object> NOT_NULL = Objects::nonNull;
	public static final Predicate<Object> ALL = o -> true;
	public static final Predicate<Object> NOT_BLANK = o -> o != null && (!(o instanceof String str) || !str.isBlank());

	protected Predicate<Object> filter = NOT_EMPTY;

	public SmartQueryWrapper() {
		this((T) null);
	}

	public SmartQueryWrapper(T entity) {
		super.setEntity(entity);
		super.initNeed();
	}

	public SmartQueryWrapper(Class<T> entityClass) {
		super.setEntityClass(entityClass);
		super.initNeed();
	}

	public SmartQueryWrapper(T entity, String... columns) {
		super.setEntity(entity);
		super.initNeed();
		this.select(columns);
	}

	/**
	 * 非对外公开的构造方法,只用于生产嵌套 sql
	 *
	 * @param entityClass 本不应该需要的
	 */
	private SmartQueryWrapper(T entity, Class<T> entityClass, AtomicInteger paramNameSeq, Map<String, Object> paramNameValuePairs, MergeSegments mergeSegments, SharedString paramAlias, SharedString lastSql, SharedString sqlComment, SharedString sqlFirst) {
		super.setEntity(entity);
		super.setEntityClass(entityClass);
		this.paramNameSeq = paramNameSeq;
		this.paramNameValuePairs = paramNameValuePairs;
		this.expression = mergeSegments;
		this.paramAlias = paramAlias;
		this.lastSql = lastSql;
		this.sqlComment = sqlComment;
		this.sqlFirst = sqlFirst;
	}

	/**
	 * 检查 SQL 注入过滤
	 */
	private boolean checkSqlInjection;

	/**
	 * 开启检查 SQL 注入
	 */
	public SmartQueryWrapper<T> checkSqlInjection() {
		this.checkSqlInjection = true;
		return this;
	}

	@Override
	protected String columnToString(String column) {
		if (checkSqlInjection && SqlInjectionUtils.check(column)) {
			throw new MybatisPlusException("Discovering SQL injection column: " + column);
		}
		return column;
	}

	@Override
	public SmartQueryWrapper<T> select(boolean condition, List<String> columns) {
		if (condition && CollectionUtils.isNotEmpty(columns)) {
			this.sqlSelect.setStringValue(String.join(StringPool.COMMA, columns));
		}
		return typedThis;
	}

	@Override
	public SmartQueryWrapper<T> select(Class<T> entityClass, Predicate<TableFieldInfo> predicate) {
		super.setEntityClass(entityClass);
		this.sqlSelect.setStringValue(TableInfoHelper.getTableInfo(getEntityClass()).chooseSelect(predicate));
		return typedThis;
	}

	@Override
	public String getSqlSelect() {
		return sqlSelect.getStringValue();
	}

	/**
	 * 用于生成嵌套 sql
	 * <p>
	 * 故 sqlSelect 不向下传递
	 * </p>
	 */
	@Override
	protected SmartQueryWrapper<T> instance() {
		return new SmartQueryWrapper<>(getEntity(), getEntityClass(), paramNameSeq, paramNameValuePairs, new MergeSegments(), paramAlias, SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString());
	}

	@Override
	public void clear() {
		super.clear();
		sqlSelect.toNull();
	}

	/**
	 * 仅添加 参数值不为空（ null 或 空字符串 ） 的约束条件
	 */
	public SmartQueryWrapper<T> filterNotEmpty() {
		this.filter = NOT_EMPTY;
		return this;
	}

	/**
	 * 仅添加 参数值不为 null 的约束条件
	 */
	public SmartQueryWrapper<T> filterNotNull() {
		this.filter = NOT_NULL;
		return this;
	}

	/**
	 * 仅添加 参数值不为空白（ null 或 空字符串 或 空白字符串 ） 的约束条件
	 */
	public SmartQueryWrapper<T> filterNotBlank() {
		this.filter = NOT_BLANK;
		return this;
	}

	/**
	 * 添加 所有 约束条件
	 */
	public SmartQueryWrapper<T> filterAll() {
		this.filter = ALL;
		return this;
	}

	@Override
	public SmartQueryWrapper<T> eq(String column, Object val) {
		return super.eq(filter.test(val), column, val);
	}

	@Override
	public SmartQueryWrapper<T> like(String column, Object val) {
		if (filter.test(val)) {
			return super.like(column, escapeSQLLike(val));
		}
		return this;
	}

	@Override
	public SmartQueryWrapper<T> likeLeft(String column, Object val) {
		if (filter.test(val)) {
			return super.likeLeft(column, escapeSQLLike(val));
		}
		return this;
	}

	@Override
	public SmartQueryWrapper<T> likeRight(String column, Object val) {
		if (filter.test(val)) {
			return super.likeRight(column, escapeSQLLike(val));
		}
		return this;
	}

	@Override
	public SmartQueryWrapper<T> notLike(String column, Object val) {
		if (filter.test(val)) {
			return super.notLike(column, escapeSQLLike(val));
		}
		return this;
	}

	@Override
	public SmartQueryWrapper<T> notLikeLeft(String column, Object val) {
		if (filter.test(val)) {
			return super.notLikeLeft(column, escapeSQLLike(val));
		}
		return this;
	}

	public static Object escapeSQLLike(Object val) {
		if (val == null) {
			return null;
		} else if (val instanceof Number) {
			return val;
		}
		return StringUtil.escapeSQLLike(val.toString());
	}

	@Override
	public SmartQueryWrapper<T> notLikeRight(String column, Object val) {
		if (filter.test(val)) {
			return super.notLikeRight(column, escapeSQLLike(val));
		}
		return this;
	}

	@Override
	public SmartQueryWrapper<T> gt(String column, Object val) {
		return super.gt(filter.test(val), column, val);
	}

	@Override
	public SmartQueryWrapper<T> ge(String column, Object val) {
		return super.ge(filter.test(val), column, val);
	}

	@Override
	public SmartQueryWrapper<T> le(String column, Object val) {
		return super.le(filter.test(val), column, val);
	}

	@Override
	public SmartQueryWrapper<T> lt(String column, Object val) {
		return super.lt(filter.test(val), column, val);
	}

	@Override
	public SmartQueryWrapper<T> ne(String column, Object val) {
		return super.ne(filter.test(val), column, val);
	}

	@Override
	public SmartQueryWrapper<T> in(String column, Object... values) {
		return super.in(values != null && values.length > 0, column, values);
	}

	@Override
	public SmartQueryWrapper<T> notIn(String column, Object... values) {
		return super.notIn(values != null && values.length > 0, column, values);
	}

	@Override
	public SmartQueryWrapper<T> notIn(String column, Collection<?> values) {
		return super.notIn(values != null && !values.isEmpty(), column, values);
	}

	@Override
	public SmartQueryWrapper<T> between(String column, @Nullable Object val1, @Nullable Object val2) {
		final boolean begin = filter.test(val1), end = filter.test(val2);
		if (begin) {
			if (end) {
				return super.between(column, val1, val2);
			} else {
				return super.ge(column, val1);
			}
		} else if (end) {
			return super.le(column, val2);
		}
		return this;
	}


	/**
	 * 添加 指定时间点 在两个字段组成的时间区间范围内 的查询条件
	 * <pre><code>
	 * begin_column <= $baseTime AND end_column >= $baseTime
	 * </code></pre>
	 */
	public SmartQueryWrapper<T> inRange(@Nullable String beginColumn, @Nullable String endColumn, @Nullable Date baseTime) {
		if (baseTime != null) {
			this.le(true, beginColumn, baseTime);
			this.ge(true, endColumn, baseTime);
		}
		return this;
	}

	@SafeVarargs
	public final <E> SmartQueryWrapper<T> ins(String column, E... values) {
		return super.in(values != null && values.length > 0, column, (Object[]) values);
	}

	public final <E> SmartQueryWrapper<T> ins(String column, Collection<E> values) {
		return super.in(values != null && !values.isEmpty(), column, values);
	}

	@Override
	public SmartQueryWrapper<T> in(String column, Collection<?> coll) {
		return super.in(coll != null && !coll.isEmpty(), column, coll);
	}

	public SmartQueryWrapper<T> limitOne() {
		return super.last("LIMIT 1");
	}

}
