package com.youyu.framework.datasource.mybatis;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Supplier;

import com.youyu.common.util.Assert;
import org.jspecify.annotations.Nullable;

/**
 * 可关联的数据实体的标识接口<br>
 * 如无特殊情况，所有的可用于进行关联记录的实体都应该实现该接口
 */
public interface Bean<K extends Serializable> extends ID {

	/**
	 * 获得实体对象的主键
	 *
	 * @return 返回值一般为整数类型，出于兼容考虑，也支持字符串等实现序列化接口的任意类型
	 */
	@Override
	K getId();

	void setId(K id);

	/**
	 * 指示当前对象是否可以与指定实例视作相同的类型
	 *
	 * @param other 指定 ID 对象
	 */
	default boolean considerAsSameType(Bean<K> other) {
		return this == other || other != null && getClass(this) == getClass(other);
	}

	/**
	 * 判断当前对象与指定 ID 对象的 id 属性是否相等（不能为 null ）
	 *
	 * @param obj 指定 ID 对象
	 */
	default boolean eqId(@Nullable Bean<K> obj) {
		if (obj != null) {
			K id = getId();
			return id != null && (this == obj || id.equals(obj.getId()) && considerAsSameType(obj));
		}
		return false;
	}

	static <K extends Serializable, T extends Bean<K>> T of(Function<? super K, T> constructor, K id) {
		return id == null ? null : constructor.apply(id);
	}

	/**
	 * 指示指定的对象是否存在有效的 ID 值
	 */
	static boolean hasId(@Nullable Bean<? extends Serializable> obj) {
		return obj != null && obj.hasValidId();
	}

	/**
	 * 指示 指定的 id 是否有效( {@code id != null && id > 0} )
	 */
	static boolean isValidId(@Nullable Number id) {
		return id != null && id.longValue() > 0;
	}

	/**
	 * 指示 指定的 id 是否有效( {@code id != null && id > 0} )
	 */
	static boolean isValidId(Integer id) {
		return id != null && id > 0;
	}

	/**
	 * 指示 指定的 id 是否有效( {@code id != null && id > 0} )
	 */
	static boolean isValidId(@Nullable String id) {
		return id != null && !id.isEmpty();
	}

	/**
	 * 获取指定对象的 id 值，如果为空则返回 null
	 */
	static <K extends Serializable> K idOf(@Nullable Bean<K> obj) {
		return obj == null ? null : obj.getId();
	}

	@SuppressWarnings("unchecked")
	default <T extends Bean<K>> T withId(K id) {
		setId(id);
		return (T) this;
	}

	static <K extends Serializable, T extends Bean<K>> T of(Supplier<T> constructor, @Nullable K id) {
		if (id == null) {
			return null;
		}
		T t = constructor.get();
		t.setId(id);
		return t;
	}

	@Nullable
	static Long toLongId(@Nullable Bean<? extends Number> obj) {
		return obj == null ? null : toLong(obj.getId());
	}

	@Nullable
	static Long toLong(@Nullable Number id) {
		if (id == null) {
			return null;
		}
		return id instanceof Long ? (Long) id : id.longValue();
	}

	@Nullable
	static Integer toIntId(@Nullable Bean<? extends Number> obj) {
		return obj == null ? null : toInt(obj.getId());
	}

	@Nullable
	static Integer toInt(@Nullable Number id) {
		if (id == null) {
			return null;
		}
		return id instanceof Integer ? (Integer) id : Math.toIntExact(id.longValue());
	}

	static String entityName(Class<?> clazz) {
		return clazz.getSimpleName();
	}

	/**
	 * 判断两个ORM实体对象的主键ID是否相等
	 */
	static <K extends Serializable, E extends Bean<K>> boolean eqId(@Nullable E o1, @Nullable E o2, boolean classEqRequired) {
		if (o1 != null && o2 != null) {
			K id = o1.getId();
			if (id == null) {
				return false;
			}
			if (o1 == o2) {
				return true;
			}
			return (!classEqRequired || getClass(o1) == getClass(o2)) && id.equals(o2.getId());
		}
		return false;
	}

	/**
	 * 判断两个ORM实体对象的主键ID是否相等
	 */
	static <K extends Serializable, E extends Bean<K>> boolean eqId(@Nullable E o1, @Nullable E o2) {
		return eqId(o1, o2, true);
	}

	/**
	 * 判断两个主键ID是否相等
	 */
	default boolean eqId(@Nullable K targetId) {
		return targetId != null && targetId.equals(getId());
	}

	@SuppressWarnings("unchecked")
	static <T> Class<T> getClass(Object entity) {
		return (Class<T>) Extension.getClassProxy.apply(entity);
	}

	/** 新建一个实例 */
	@SuppressWarnings("unchecked")
	static <T> T newInstance(Class<T> clazz) {
		return (T) Extension.newInstanceProxy.apply(clazz);
	}

	class Extension {

		static Function<Object, Class<?>> getClassProxy = Object::getClass;

		@SuppressWarnings("deprecation")
		static Function<Class<?>, ?> newInstanceProxy = clazz -> {
			try {
				return clazz.newInstance();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		};

		public static void setGetClassProxy(@Nullable Function<Object, Class<?>> proxy) {
			Assert.notNull(proxy);
			getClassProxy = proxy;
		}

		public static void setNewInstanceProxy(@Nullable Function<Class<?>, ?> proxy) {
			Assert.notNull(proxy);
			newInstanceProxy = proxy;
		}

	}

}
