package com.youyu.framework.datasource.mybatis;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.reflect.GenericTypeUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.youyu.common.util.Assert;
import com.youyu.common.util.ObjUtil;
import com.youyu.framework.context.Env;
import com.youyu.framework.context.web.util.SpringUtil;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.youyu.framework.datasource.mybatis.SmartQueryWrapper;

public abstract class BaseRepositoryImpl<T extends Bean<K>, D extends BaseDao<T>, K extends Serializable> implements BaseRepository<T, K> {

	protected static final Log mbpLog = LogFactory.getLog(BaseRepository.class);

	private static SqlSessionFactory sqlSessionFactory;

	protected D baseDao;

	protected final Class<T> entityClass;
	protected final Class<D> mapperClass;

	@SuppressWarnings("unchecked")
	public BaseRepositoryImpl() {
		final Class<?>[] typeArguments = GenericTypeUtils.resolveTypeArguments(getClass(), BaseRepositoryImpl.class);
		entityClass = (Class<T>) typeArguments[0];
		mapperClass = (Class<D>) typeArguments[1];
	}

	@Autowired
	public void setBaseDao(D baseDao) {
		this.baseDao = baseDao;
	}

	protected static SqlSessionFactory getSqlSessionFactory() {
		if (sqlSessionFactory == null) {
			sqlSessionFactory = SpringUtil.getBeanByNameFirst(SqlSessionFactory.class);
		}
		return sqlSessionFactory;
	}

	protected static void checkIdRequired(ID entity) {
		Assert.isTrue(ID.hasId(entity));
	}

	@Override
	public T get(K id) {
		if (id == null) {
			return null;
		}
		return baseDao.selectById(id);
	}

	@Override
	public T getAny() {
		return baseDao.selectOne(new QueryWrapper<T>().last("LIMIT 1"));
	}

	@Override
	public T getByIdForUpdate(K id) {
		if (id == null) {
			return null;
		}
		QueryWrapper<T> wrapper = new QueryWrapper<T>()
				.eq(BaseDO.ID, id)
				.last("FOR UPDATE");

		return baseDao.selectOne(wrapper);
	}

	/**
	 * 根据主键ID数组获取对应的实体数据
	 */
	@Override
	public List<T> findByIds(K... ids) {
		return findByIds(Arrays.asList(ids));
	}

	/**
	 * 根据主键ID数组获取对应的实体数据
	 */
	@Override
	public List<T> findByIds(Collection<K> ids) {
		if (ids.isEmpty()) {
			return new ArrayList<>();
		}
		return baseDao.selectBatchIds(ids);
	}

	protected static <E> SmartQueryWrapper<E> smartWrapEq(String field, @Nullable Object value) {
		return new SmartQueryWrapper<E>()
				.eq(field, value);
	}

	protected static <E> SmartQueryWrapper<E> smartWrapEq(String field1, @Nullable Object value1, String field2, @Nullable Object value2) {
		return new SmartQueryWrapper<E>()
				.eq(field1, value1)
				.eq(field2, value2);
	}

	protected static <E> SmartQueryWrapper<E> smartWrapEq(String field1, @Nullable Object value1,
	                                                      String field2, @Nullable Object value2,
	                                                      String field3, @Nullable Object value3) {
		return new SmartQueryWrapper<E>()
				.eq(field1, value1)
				.eq(field2, value2)
				.eq(field3, value3);
	}

	protected static <E> SmartQueryWrapper<E> smartWrapEq(String field1, @Nullable Object value1,
	                                                      String field2, @Nullable Object value2,
	                                                      String field3, @Nullable Object value3, Object... morePairs) {
		SmartQueryWrapper<E> wrapper = smartWrapEq(field1, value1, field2, value2, field3, value3);
		if (morePairs != null) {
			for (int i = 0; i < morePairs.length; i++) {
				wrapper.eq((String) morePairs[i++], morePairs[i]);
			}
		}
		return wrapper;
	}

	protected SmartQueryWrapper<T> smartQueryWrapper() {
		return new SmartQueryWrapper<>();
	}

	protected SmartQueryWrapper<T> smartEq(String field, @Nullable Object value) {
		return smartQueryWrapper().eq(field, value);
	}

	protected SmartQueryWrapper<T> smartEq(String field1, @Nullable Object value1,
	                                       String field2, @Nullable Object value2) {
		return smartQueryWrapper().eq(field1, value1).eq(field2, value2);
	}

	protected SmartQueryWrapper<T> smartEq(String field1, @Nullable Object value1,
	                                       String field2, @Nullable Object value2,
	                                       String field3, @Nullable Object value3) {
		return smartQueryWrapper().eq(field1, value1).eq(field2, value2).eq(field3, value3);
	}

	protected SmartQueryWrapper<T> smartEq(String field1, @Nullable Object value1,
	                                       String field2, @Nullable Object value2,
	                                       String field3, @Nullable Object value3, Object... morePairs) {
		return smartWrapEq(field1, value1, field2, value2, field3, value3, morePairs);
	}

	protected QueryWrapper<T> queryWrapper() {
		return new QueryWrapper<>();
	}

	protected UpdateWrapper<T> updateWrapper() {
		return new UpdateWrapper<>();
	}

	protected List<T> findByField(String field, Object value) {
		var wrapper = new QueryWrapper<T>()
				.eq(field, value);
		return baseDao.selectList(wrapper);
	}

	/**
	 * 根据指定的字段获取对应的唯一的持久化实例。<br>
	 * 如果没有对应的实例，则返回 null。
	 */
	protected T getUniqueByField(String uniqueField, Object value) {
		QueryWrapper<T> wrapper = new QueryWrapper<T>()
				.eq(uniqueField, value);
		return baseDao.selectOne(wrapper);
	}

	/**
	 * 根据 用户ID 查询关联的实体对象
	 */
	protected T getByUserId(@NonNull Long userId) {
		Assert.notNull(userId);
		QueryWrapper<T> wrapper = new QueryWrapper<T>()
				.eq("user_id", userId);
		return baseDao.selectOne(wrapper);
	}

	/**
	 * 获取指定实体类的所有实体对象的集合，并以指定的方式进行排序
	 */
	public List<T> findAll() {
		return baseDao.selectList(null);
	}

	/**
	 * 添加指定的对象(到对应的数据库表中)
	 */
	@Transactional
	public void save(T entity) {
		baseDao.insert(entity);
	}

	@Transactional
	public boolean saveBatch(Collection<T> list) {
		return saveBatch(list, IService.DEFAULT_BATCH_SIZE);
	}

	@Transactional
	public boolean saveBatch(Collection<T> list, int batchSize) {
		if (list.isEmpty()) {
			return false;
		}
		String sqlStatement = SqlHelper.getSqlStatement(mapperClass, SqlMethod.INSERT_ONE);
		return SqlHelper.executeBatch(getSqlSessionFactory(), mbpLog, list, batchSize, (sqlSession, entity) -> sqlSession.insert(sqlStatement, entity));
	}

	/**
	 * 更新指定的对象
	 */
	@Override
	@Transactional
	public int update(T entity) {
		return baseDao.updateById(entity);
	}

	/**
	 * 更新指定的对象
	 */
	@Override
	@Transactional
	public int update(T entity, Wrapper<T> updateWrapper) {
		return baseDao.update(entity, updateWrapper);
	}

	@Transactional
	public int update(Wrapper<T> updateWrapper) {
		return baseDao.update(updateWrapper);
	}

	@Transactional
	public boolean updateBatchById(Collection<T> list) {
		return updateBatchById(list, IService.DEFAULT_BATCH_SIZE);
	}

	@Transactional
	public boolean updateBatchById(Collection<T> list, int batchSize) {
		String sqlStatement = SqlHelper.getSqlStatement(mapperClass, SqlMethod.UPDATE_BY_ID);
		return SqlHelper.executeBatch(getSqlSessionFactory(), mbpLog, list, batchSize, (sqlSession, entity) -> {
			MapperMethod.ParamMap<T> param = new MapperMethod.ParamMap<>();
			param.put(Constants.ENTITY, entity);
			sqlSession.update(sqlStatement, param);
		});
	}

	@Transactional
	public boolean updateBatchByWrappers(Collection<UpdateWrapper<T>> wrappers) {
		return updateBatchByWrappers(wrappers, IService.DEFAULT_BATCH_SIZE);
	}

	@Transactional
	public boolean updateBatchByWrappers(Collection<UpdateWrapper<T>> wrappers, int batchSize) {
		final String sqlStatement = SqlHelper.getSqlStatement(mapperClass, SqlMethod.UPDATE);
		return SqlHelper.executeBatch(getSqlSessionFactory(), mbpLog, wrappers, batchSize,
				(sqlSession, wrapper) -> sqlSession.update(sqlStatement, Map.of(Constants.WRAPPER, wrapper)));
	}

	/**
	 * 添加、更新指定的对象
	 */
	protected void saveOrUpdate(T entity, boolean hasId) {
		if (hasId) {
			update(entity);
			return;
		}
		save(entity);
	}

	/**
	 * 添加、更新指定的对象【设置空值使用】
	 *
	 * @param entity 实体对象
	 * @param hasId 是否编辑
	 * @param fieldAndValues 修改的属性【存在相同属性，以该属性为主】
	 */
	protected void saveOrUpdate(T entity, boolean hasId, Object... fieldAndValues) {
		if (hasId) {
			UpdateWrapper<T> wrapper = new UpdateWrapper<T>()
					.eq(BaseDO.ID, entity.getId());
			if (ObjUtil.isValid(fieldAndValues)) {
				for (int i = 0; i < fieldAndValues.length; i++) {
					wrapper.set((String) fieldAndValues[i++], fieldAndValues[i]);
				}
			}
			update(entity, wrapper);
			return;
		}
		this.save(entity);
	}

	/**
	 * 根据条件查询对应的实体数据
	 */
	protected T selectOne(Wrapper<T> queryWrapper) {
		return baseDao.selectOne(queryWrapper);
	}

	public <W extends AbstractWrapper<T, String, W>> T getFirst(W wrapper) {
		wrapper.last("LIMIT 1");
		return baseDao.selectOne(wrapper);
	}

	/**
	 * 根据条件查询对应的实体数
	 */
	protected Long count(Wrapper<T> queryWrapper) {
		return baseDao.selectCount(queryWrapper);
	}

	/**
	 * 指示是否存在符合条件的记录
	 */
	public <W extends AbstractWrapper<T, String, W> & Query<W, T, String>> boolean exists(W queryWrapper) {
		queryWrapper.select("1").last("LIMIT 1");
		return !baseDao.selectObjs(queryWrapper).isEmpty();
	}

	/**
	 * 根据条件查询对应的实体数据
	 */
	protected List<T> selectList(Wrapper<T> queryWrapper) {
		return baseDao.selectList(queryWrapper);
	}

	/**
	 * 根据条件查询对应的实体数据并分页
	 */
	protected Page<T> selectPage(Page<?> page, Wrapper<T> queryWrapper) {
		if (page.getSize() < 0) {
			List<T> list = baseDao.selectList(queryWrapper);
			int size = list != null ? list.size() : 0;
			return new Page<T>(1, size, size).setRecords(list);
		}
		return baseDao.selectPage(ObjUtil.castType(page), queryWrapper);
	}

	/**
	 * 从数据存储中移除指定的实体对象
	 */
	@Transactional
	public boolean delete(T entity) {
		Assert.notNull(Bean.idOf(entity));
		return deleteById(entity.getId());
	}

	/**
	 * 从数据存储中移除指定的实体对象
	 */
	@Transactional
	public boolean deleteById(K id) {
		return baseDao.deleteById(id) > 0;
	}


	/**
	 * 从数据存储中批量移除指定的实体对象
	 */
	@Transactional
	public int deleteByIds(K... ids) {
		return deleteByIds(Arrays.asList(ids));
	}

	/**
	 * 从数据存储中批量移除指定的实体对象
	 */
	@Transactional
	public int deleteByIds(Collection<K> ids) {
		return baseDao.deleteBatchIds(ids);
	}

	/**
	 * 从数据存储中移除符合指定条件的数据
	 */
	protected int delete(UpdateWrapper<T> wrapper) {
		return baseDao.delete(wrapper);
	}

	/**
	 * 更新符合指定条件的数据
	 */
	protected int update(UpdateWrapper<T> wrapper) {
		Assert.notNull(wrapper);
		return baseDao.update(null, wrapper);
	}

	/**
	 * 更新符合指定条件的数据
	 */
	protected int update(T toUpdate, UpdateWrapper<T> wrapper) {
		Assert.isTrue(toUpdate != null || wrapper != null);
		return baseDao.update(toUpdate, wrapper);
	}

	/**
	 * 判断对象表中是否存在指定属性的值
	 *
	 * @param moreFieldAndValues 键值对(键必须为字符串)
	 * @since 1.0
	 */
	public boolean exists(String field, Object value, Object... moreFieldAndValues) {
		var wrapper = new QueryWrapper<T>()
				.eq(field, value);
		for (int i = 0; i < moreFieldAndValues.length; i++) {
			wrapper.eq((String) moreFieldAndValues[i++], moreFieldAndValues[i]);
		}
		return exists(wrapper);
	}

	private static final ConcurrentMap<String, String> incrExprMap = new ConcurrentHashMap<>();
	private static final Function<String, String> exprFunction = f -> f + "=" + f + "+ {0}";

	protected static <T> void incrSetIfHasValue(UpdateWrapper<T> wrapper, @NonNull String column, @Nullable Number delta) {
		if (delta == null) {
			return;
		}
		// a = a + {0} 使 MySQL 缓存最近最常用的SQL
		wrapper.setSql(true, incrExprMap.computeIfAbsent(column, exprFunction), delta);
	}

	protected static <T> void incrSetValue(UpdateWrapper<T> wrapper, @NonNull String column, @NonNull Number delta) {
		// a = a + {0} 使 MySQL 缓存最近最常用的SQL
		wrapper.setSql(true, incrExprMap.computeIfAbsent(column, exprFunction), delta);
	}

	static final int SINGLE_QUERY_SIZE = Env.inProduction() ? 10000 : 500;

	/**
	 * 根据传入的参数列表进行分批查询，避免参数列表过多单次查询超时，一般用于导出全量数据的情况
	 */
	protected <E, R> List<R> batchFind(@NonNull Collection<E> all, @NonNull Function<List<E>, List<R>> queryHandler) {
		final int totalSize = all.size();
		if (totalSize == 0) {
			return Collections.emptyList();
		}
		final List<R> result = new ArrayList<>(totalSize);
		final List<E> part = new ArrayList<>(Math.min(SINGLE_QUERY_SIZE, totalSize));
		int i = 0;
		for (E param : all) {
			part.add(param);
			if (++i % SINGLE_QUERY_SIZE == 0 || i == totalSize) {
				List<R> list = queryHandler.apply(part);
				if (ObjUtil.isValid(list)) {
					result.addAll(list);
				}
				part.clear();
			}
		}
		return result;
	}

	/**
	 * 根据传入的参数列表进行分批查询，适用于参数拼接的方式
	 */
	protected <R> List<R> batchAppendFind(@NonNull Collection<Long> ids, @NonNull Function<String, List<R>> findFunc) {
		final int totalSize = ids.size();
		if (totalSize == 0) {
			return Collections.emptyList();
		}
		List<R> result = new ArrayList<>(totalSize);
		final StringBuilder sb = new StringBuilder(Math.min(SINGLE_QUERY_SIZE, totalSize) * 6);
		int i = 0;
		for (Long id : ids) {
			sb.append(id.longValue());
			if (++i % SINGLE_QUERY_SIZE == 0 || totalSize == i) {
				List<R> list = findFunc.apply(sb.toString());
				if (ObjUtil.isValid(list)) {
					result.addAll(list);
				}
				sb.setLength(0);
			} else {
				sb.append(",");
			}
		}
		return result;
	}

}