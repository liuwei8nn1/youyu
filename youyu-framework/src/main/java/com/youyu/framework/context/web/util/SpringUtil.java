package com.youyu.framework.context.web.util;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.*;

import com.youyu.common.functional.AnyRunnable;
import com.youyu.common.functional.AnySupplier;
import com.youyu.common.util.ObjUtil;
import com.youyu.framework.warn.core.MsgWarnChannel;
import com.youyu.framework.context.Env;
import jodd.util.StringUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.*;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Spring辅助工具类，主要用于在任何地方获取Spring管理的Bean对象
 */
@Component
public class SpringUtil implements ApplicationContextAware {

	public static final String TASK_EXECUTOR_BEAN_NAME = "defaultTaskExecutor";
	public static final Logger log = LoggerFactory.getLogger(SpringUtil.class);

	// Spring应用上下文环境
	static ApplicationContext applicationContext;
	static TaskExecutor taskExecutor;
	static boolean replaceIfNecessary = true;

	@Nullable
	static MsgWarnChannel msgWarnChannel;

	@Autowired(required = false)
	public void setMsgWarnChannel(MsgWarnChannel channel) {
		msgWarnChannel = channel;
	}

	public static void setGlobalApplicationContext(ApplicationContext context) {
		replaceIfNecessary = true;
		applicationContext = context;
	}

	/**
	 * 实现ApplicationContextAware接口的回调方法，设置上下文环境
	 */
	@Override
	public void setApplicationContext(ApplicationContext context) {
		applicationContext = context;
	}

	/**
	 * 获取ApplicationContext对象
	 *
	 * @return ApplicationContext
	 */
	public static ApplicationContext getApplicationContext() {
		// if (replaceIfNecessary) {
		// 	ApplicationContext context = Context.get().getApplicationContext();
		// 	if (context != null) {
		// 		replaceIfNecessary = false;
		// 		applicationContext = context;
		// 	}
		// }
		return applicationContext;
	}

	/**
	 * 检测Spring容器中是否存在指定名称的Bean对象
	 *
	 * @param name Spring管理的bean的名称
	 * @return boolean 如果存在返回true，否则返回false
	 */
	public static boolean containsBean(String name) {
		return getApplicationContext().containsBean(name);
	}

	/**
	 * 检测Spring容器中是否存在指定名称的Bean对象
	 *
	 * @param clazz Spring 管理的 bean 类型
	 * @return boolean 如果存在返回 true，否则返回 false
	 */
	public static boolean containsBean(Class<?> clazz) {
		String[] names = getApplicationContext().getBeanNamesForType(clazz);
		return names != null && names.length > 0;
	}

	/**
	 * 根据类型获取 Spring 管理的 Bean 对象
	 *
	 * @param clazz Spring 管理的 bean 的类型
	 * @return defaultBeanLoader 返回默认对象的 Supplier
	 */
	public static <T> T getBeanOrDefault(Class<T> clazz, @Nullable Supplier<T> defaultBeanLoader) {
		ApplicationContext context = getApplicationContext();
		String[] names = context.getBeanNamesForType(clazz);
		if (names != null && names.length > 0) {
			return context.getBean(clazz);
		}
		return defaultBeanLoader == null ? null : defaultBeanLoader.get();
	}

	/**
	 * 根据指定的名称获取Spring管理的Bean对象
	 *
	 * @param name Spring管理的bean的名称
	 * @return Object 返回Spring管理的bean实例，如果指定实例不存在将抛出异常
	 */
	public static <T> T getBean(String name) {
		return ObjUtil.castType(getApplicationContext().getBean(name));
	}

	/**
	 * 根据指定的名称获取Spring管理的Bean对象
	 *
	 * @param name Spring管理的bean的名称
	 * @param args 获取该Bean实例所需的构造方法或工厂方法的参数列表
	 * @return Object 返回Spring管理的bean实例，如果指定实例不存在将抛出异常
	 */
	public static <T> T getBean(String name, Object... args) {
		return ObjUtil.castType(getApplicationContext().getBean(name, args));
	}

	/**
	 * 根据类型获取Spring管理的Bean对象
	 *
	 * @param clazz Spring管理的bean的类型
	 * @return Object 返回Spring管理的bean实例，如果指定实例不存在或存在重复的类型冲突将抛出异常
	 */
	public static <T> T getBean(Class<T> clazz) {
		return getApplicationContext().getBean(clazz);
	}

	/**
	 * 先根据类型名称作为 beanName 去获取对应类型的实例
	 * 如果找不到匹配的示例，则再根据类型去获取实例
	 *
	 * @param clazz Spring管理的bean的类型
	 * @return Object 返回Spring管理的bean实例，如果指定实例不存在或存在重复的类型冲突将抛出异常
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBeanByNameFirst(Class<T> clazz) {
		ApplicationContext context = getApplicationContext();
		String beanName = Introspector.decapitalize(clazz.getSimpleName());
		try {
			Object bean = context.getBean(beanName);
			if (clazz.isAssignableFrom(bean.getClass())) {
				return (T) bean;
			}
		} catch (BeansException ignored) {
		}
		return context.getBean(clazz);
	}

	/**
	 * 根据类型获取Spring管理的Bean对象
	 *
	 * @param clazz Spring管理的bean的类型
	 * @param args 获取该Bean实例所需的构造方法或工厂方法的参数列表
	 * @return Object 返回Spring管理的bean实例，如果指定实例不存在或存在重复的类型冲突将抛出异常
	 */
	public static <T> T getBean(Class<T> clazz, Object... args) {
		return getApplicationContext().getBean(clazz, args);
	}

	/**
	 * 根据类型获取Spring容器指定类型的Bean对象的Map集合。键为Bean的Name，值为对应的Bean实例。
	 *
	 * @param clazz Spring管理的bean的类型
	 */
	public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
		return getApplicationContext().getBeansOfType(clazz);
	}

	/**
	 * 根据泛型获取Spring容器具有指定注解的Bean对象的Map集合。键为Bean的Name，值为对应的Bean实例。
	 *
	 * @param annotationType Spring管理的bean所具备的的注解类型
	 */
	public static Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
		return getApplicationContext().getBeansWithAnnotation(annotationType);
	}

	/**
	 * 发布(触发)指定的应用事件
	 *
	 * @since 7.0
	 */
	public static void publishEvent(ApplicationEvent event) {
		getApplicationContext().publishEvent(event);
	}

	/**
	 * 主动向 Spring 容器中注册 bean
	 *
	 * @param applicationContext Spring容器
	 * @param name BeanName
	 * @param clazz 注册的 bean 的类性
	 * @param callback 在构建 BeanDefinitionBuilder 之前的最后回调处理
	 * @param constructorArgs 构造方法的必要参数，顺序和类型要求和clazz中定义的一致
	 * @return 返回注册到容器中的bean对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getOrRegisterBean(ConfigurableApplicationContext applicationContext, String name, Class<? extends T> clazz,
	                                      @Nullable Consumer<BeanDefinitionBuilder> callback, Object... constructorArgs) {
		if (applicationContext.containsBean(name)) {
			Object bean = applicationContext.getBean(name);
			if (bean.getClass().isAssignableFrom(clazz)) {
				return (T) bean;
			}
			throw new IllegalArgumentException("Cannot register duplicate bean name: " + name);
		}
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
		for (Object arg : constructorArgs) {
			builder.addConstructorArgValue(arg);
		}

		if (callback != null) {
			callback.accept(builder);
		}

		BeanDefinition beanDefinition = builder.getRawBeanDefinition();
		BeanDefinitionRegistry beanFactory = (BeanDefinitionRegistry) applicationContext.getBeanFactory();
		beanFactory.registerBeanDefinition(name, beanDefinition);
		return applicationContext.getBean(name, clazz);
	}

	/**
	 * 主动向 Spring 容器中注册 bean
	 *
	 * @param applicationContext Spring容器
	 * @param name BeanName
	 * @param clazz 注册的 bean 的类性
	 * @param constructorArgs 构造方法的必要参数，顺序和类型要求和clazz中定义的一致
	 * @return 返回注册到容器中的bean对象
	 */
	public static <T> T getOrRegisterBean(ConfigurableApplicationContext applicationContext, String name, Class<? extends T> clazz,
	                                      Object... constructorArgs) {
		return getOrRegisterBean(applicationContext, name, clazz, null, constructorArgs);

	}

	/**
	 * 主动向 Spring 容器中注册 bean
	 *
	 * @param name BeanName
	 * @param clazz 注册的 bean 的类性
	 * @param callback 在构建 BeanDefinitionBuilder 之前的最后回调处理
	 * @param constructorArgs 构造方法的必要参数，顺序和类型要求和clazz中定义的一致
	 * @return 返回注册到容器中的bean对象
	 */
	public static <T> T getOrRegisterBean(String name, Class<? extends T> clazz, @Nullable Consumer<BeanDefinitionBuilder> callback, Object... constructorArgs) {
		return getOrRegisterBean((ConfigurableApplicationContext) getApplicationContext(), name, clazz, callback, constructorArgs);
	}

	/**
	 * 主动向 Spring 容器中注册 bean
	 *
	 * @param name BeanName
	 * @param clazz 注册的 bean 的类性
	 * @param constructorArgs 构造方法的必要参数，顺序和类型要求和clazz中定义的一致
	 * @return 返回注册到容器中的bean对象
	 */
	public static <T> T getOrRegisterBean(String name, Class<? extends T> clazz, Object... constructorArgs) {
		return getOrRegisterBean((ConfigurableApplicationContext) getApplicationContext(), name, clazz, null, constructorArgs);
	}

	public static TaskExecutor getTaskExecutor() {
		if (taskExecutor == null) {
			taskExecutor = getApplicationContext().getBean(TASK_EXECUTOR_BEAN_NAME, TaskExecutor.class);
		}
		return taskExecutor;
	}

	/**
	 * 异步执行指定的 Runnable（内部自带异常 try-catch-logger）
	 *
	 * @param errorMsgOrSupplier 提示信息（ String）或 {@code Supplier<String>}
	 */
	public static void asyncRun(AnyRunnable task, @Nullable Runnable finallyTask, @NonNull Logger logger, @Nullable Object errorMsgOrSupplier) {
		if (finallyTask == null) {
			getTaskExecutor().execute(() -> {
						try {
							task.run();
						} catch (Throwable e) {
							logErrorInternal(logger, errorMsgOrSupplier, e);
						}
					}
			);
		} else {
			getTaskExecutor().execute(() -> {
				try {
					task.run();
				} catch (Throwable e) {
					logErrorInternal(logger, errorMsgOrSupplier, e);
				} finally {
					finallyTask.run();
				}
			});
		}
	}

	/**
	 * 记录错误日志
	 *
	 * @param channel 指定上报告警通知的渠道，如果为 null，则不上报
	 */
	private static void logErrorInternal(@NonNull Logger logger, @Nullable Object errorMsgOrSupplier, @Nullable Throwable e, @Nullable MsgWarnChannel channel) {
		final String error = errorMsgOrSupplier == null ? "执行异步任务时出错！" : StringUtil.toString(ObjUtil.tryUnwrap(errorMsgOrSupplier));
		logger.error(error, e);
		if (channel != null) {
			if (e == null) {
				channel.sendBugMsg(error);
			} else {
				channel.sendBugMsg(error, e);
			}
		}
	}

	/**
	 * 记录错误日志 并上报告警通知
	 */
	private static void logErrorInternal(@NonNull Logger logger, @Nullable Object errorMsgOrSupplier, @Nullable Throwable e) {
		logErrorInternal(logger, errorMsgOrSupplier, e, msgWarnChannel);
	}

	/**
	 * 记录错误日志 并上报告警通知
	 */
	public static void logError(@NonNull Logger logger, @Nullable CharSequence errorMsg, Throwable e) {
		logErrorInternal(logger, errorMsg, e);
	}

	/**
	 * 记录错误日志 并上报告警通知
	 */
	public static void logError(@NonNull Logger logger, @Nullable Supplier<String> errorMsg, Throwable e) {
		logErrorInternal(logger, errorMsg, e);
	}

	/**
	 * 记录错误日志 并上报告警通知
	 */
	public static void logError(@NonNull Logger logger, @Nullable CharSequence errorMsg) {
		logErrorInternal(logger, errorMsg, null);
	}

	/**
	 * 记录错误日志 并上报告警通知
	 */
	public static void logError(@NonNull Logger logger, @Nullable Supplier<String> errorMsg) {
		logErrorInternal(logger, errorMsg, null);
	}

	/**
	 * 异步执行指定的 Runnable（内部自带异常 try-catch-logger）
	 */
	public static void asyncRun(AnyRunnable task, final Logger logger, @Nullable Object errorMsgOrSupplier) {
		asyncRun(task, null, logger, errorMsgOrSupplier);
	}

	/**
	 * 异步执行指定的 Runnable（内部自带异常 try-catch-logger）
	 */
	public static void asyncRun(AnyRunnable task, @Nullable Runnable finallyTask, @Nullable Object errorMsgOrSupplier) {
		asyncRun(task, finallyTask, log, errorMsgOrSupplier);
	}

	/**
	 * 异步执行指定的 Runnable（内部自带异常 try-catch-logger）
	 */
	public static void asyncRun(AnyRunnable task, String errorMsg) {
		asyncRun(task, null, log, errorMsg);
	}

	/**
	 * 异步执行指定的 Runnable（内部自带异常 try-catch-logger）
	 */
	public static void asyncRun(AnyRunnable task, @Nullable Supplier<CharSequence> errorMsgSupplier) {
		asyncRun(task, null, log, errorMsgSupplier);
	}

	/**
	 * 异步执行指定的 Runnable（内部自带异常 try-catch-logger）
	 */
	public static void asyncRun(AnyRunnable task, @Nullable Runnable finallyTask) {
		asyncRun(task, finallyTask, null);
	}

	/**
	 * 异步执行指定的 Runnable（内部自带异常 try-catch-logger）
	 */
	public static void asyncRun(AnyRunnable task) {
		asyncRun(task, null, log, null);
	}

	/**
	 * 异步执行指定的业务处理
	 */
	public static void asyncExecute(final Runnable runnable) {
		getTaskExecutor().execute(runnable);
	}

	/**
	 * 在当前事务成功提交后【同步/异步】执行指定任务
	 */
	static void doAfterCommit(AnyRunnable task, @Nullable Object errorMsgOrSupplier, boolean async) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				doTask(task, errorMsgOrSupplier, async);
			}
		});
	}

	/**
	 * 在当前事务成功提交后【同步/异步】执行指定任务
	 */
	static void doAfterCommitSmart(AnyRunnable task, @Nullable Object errorMsgOrSupplier, boolean async) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			doAfterCommit(task, errorMsgOrSupplier, async);
			return;
		}
		doTask(task, errorMsgOrSupplier, async);
	}

	/**
	 * 在当前事务成功提交后【同步/异步】执行指定任务
	 * <p>
	 * 如果当前不在事务中，则<b>立即</b>执行任务
	 */
	public static void runAfterCommitSmart(AnyRunnable task, @Nullable String errorMsg, boolean async) {
		doAfterCommitSmart(task, errorMsg, async);
	}

	/**
	 * 在当前事务成功提交后【同步/异步】执行指定任务
	 * <p>
	 * 如果当前不在事务中，则<b>立即</b>执行任务
	 */
	public static void runAfterCommitSmart(AnyRunnable task, boolean async) {
		doAfterCommitSmart(task, null, async);
	}

	/**
	 * 在当前事务成功提交后【同步/异步】执行指定任务
	 * <p>
	 * 如果当前不在事务中，则<b>立即</b>执行任务
	 */
	public static void runAfterCommitSmart(AnyRunnable task, @Nullable Supplier<String> errorMsg, boolean async) {
		doAfterCommitSmart(task, errorMsg, async);
	}

	static void doTask(AnyRunnable task, @Nullable Object errorMsgOrSupplier, boolean async) {
		if (async) { // 异步执行
			asyncRun(task, log, errorMsgOrSupplier);
			return;
		}
		try {
			task.run();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * 在当前事务成功提交后【同步/异步】执行指定任务
	 */
	public static void runAfterCommit(AnyRunnable task, @Nullable String errorMsg, boolean async) {
		doAfterCommit(task, errorMsg, async);
	}

	/**
	 * 在当前事务成功提交后【同步/异步】执行指定任务
	 */
	public static void runAfterCommit(AnyRunnable task, @Nullable Supplier<String> errorMsg, boolean async) {
		doAfterCommit(task, errorMsg, async);
	}

	/**
	 * 在当前事务成功提交后【异步】执行指定任务
	 */
	public static void asyncRunAfterCommit(AnyRunnable task, String errorMsgOrSupplier) {
		doAfterCommit(task, errorMsgOrSupplier, true);
	}

	/**
	 * 在当前事务成功提交后【异步】执行指定任务
	 */
	public static void asyncRunAfterCommit(AnyRunnable task, Supplier<String> errorMsgSupplier) {
		doAfterCommit(task, errorMsgSupplier, true);
	}

	/**
	 * 在当前事务成功提交后【异步】执行指定任务
	 *
	 * @deprecated 请使用 {@link #asyncRunAfterCommit(AnyRunnable, Supplier)} 替代
	 */
	@Deprecated
	public static void asyncRunAfterCommit(AnyRunnable task) {
		doAfterCommit(task, null, true);
	}

	/**
	 * 在当前事务成功提交后【同步】执行指定任务（如果有异常，也同步抛出）
	 */
	public static void runAfterCommit(AnyRunnable task) {
		doAfterCommit(task, null, false);
	}

	/** 从 Spring 容器中获取指定枚举对应的接口实现实例集合 */
	public static <K extends Enum<K>, V> EnumMap<K, V> newEnumImplMap(Class<K> enumClass, Class<V> interfaceClass, Function<? super V, ? extends K> keyMapper, boolean unique) {
		return newEnumImplMap(enumClass, getBeansOfType(interfaceClass).values(), keyMapper, unique);
	}

	/** 从 Spring 容器中获取指定枚举对应的接口实现实例集合 */
	public static <K extends Enum<K>, V> EnumMap<K, V> newEnumImplMap(Class<K> enumClass, @Nullable Collection<V> impls, Function<? super V, ? extends K> keyMapper, boolean unique) {
		final EnumMap<K, V> map = new EnumMap<>(enumClass);
		for (V impl : impls) {
			V old = map.put(keyMapper.apply(impl), impl);
			if (unique && old != null) {
				throw new IllegalStateException("Found duplicate: " + keyMapper.apply(impl));
			}
		}
		return map;
	}

	/** 从 Spring 容器中获取指定枚举对应的接口实现实例集合 */
	public static <K extends Enum<K>, V> EnumMap<K, V> newEnumImplMap(Class<K> enumClass, @Nullable Collection<V> impls, Function<? super V, ? extends K> keyMapper) {
		return newEnumImplMap(enumClass, impls, keyMapper, Env.inner());
	}

	/** 从 Spring 容器中获取指定枚举对应的接口实现实例集合 */
	public static <K extends Enum<K>, V> EnumMap<K, V> newEnumImplMap(Class<K> enumClass, Class<V> interfaceClass, Function<? super V, ? extends K> keyMapper) {
		return newEnumImplMap(enumClass, interfaceClass, keyMapper, Env.inner());  // 线上环境无需检查重复
	}

	protected static <T> T doRetry(int maxRetries, @Nullable Object errorMsgOrSupplier, AnySupplier<T> task, @Nullable MsgWarnChannel channel) throws Exception {
		int errorCount = 0;
		do {
			try {
				return task.get();
			} catch (Exception e) {
				if (errorCount++ < maxRetries) {
					if (errorMsgOrSupplier == null) {
						errorMsgOrSupplier = "第" + errorCount + "次执行任务时出错，正在重试！";
					} else {
						errorMsgOrSupplier = "第" + errorCount + "次" + StringUtil.toString(ObjUtil.tryUnwrap(errorMsgOrSupplier));
					}
					logErrorInternal(log, errorMsgOrSupplier, e, channel);
					continue;
				}
				throw e;
			}
		} while (true);
	}

	/**
	 * 执行指定的任务，如果报错，则进行重试，直到成功或达到最大重试次数为止
	 *
	 * @param maxRetries 最大重试次数（首次执行不算重试，不计入在内）
	 * @param channel 用于上报告警通知的渠道，可以为 null（表示不上报）
	 */
	public static <T> T retry(int maxRetries, @Nullable CharSequence errorMsgOrSupplier, AnySupplier<T> task, @Nullable MsgWarnChannel channel) throws Exception {
		return doRetry(maxRetries, errorMsgOrSupplier, task, channel);
	}

	/**
	 * 执行指定的任务，如果报错，则进行重试，直到成功或达到最大重试次数为止
	 *
	 * @param maxRetries 最大重试次数（首次执行不算重试，不计入在内）
	 * @param channel 用于上报告警通知的渠道，可以为 null（表示不上报）
	 */
	public static <T> T retry(int maxRetries, @Nullable Supplier<String> errorMsgOrSupplier, AnySupplier<T> task, @Nullable MsgWarnChannel channel) throws Exception {
		return doRetry(maxRetries, errorMsgOrSupplier, task, channel);
	}

	/**
	 * 执行指定的任务，如果报错，则进行重试，直到成功或达到最大重试次数为止
	 *
	 * @param maxRetries 最大重试次数（首次执行不算重试，不计入在内）
	 */
	public static <T> T retry(int maxRetries, @Nullable CharSequence errorMsgOrSupplier, AnySupplier<T> task) throws Exception {
		return doRetry(maxRetries, errorMsgOrSupplier, task, msgWarnChannel);
	}

	/**
	 * 执行指定的任务，如果报错，则进行重试，直到成功或达到最大重试次数为止
	 *
	 * @param maxRetries 最大重试次数（首次执行不算重试，不计入在内）
	 */
	public static <T> T retry(int maxRetries, @Nullable Supplier<String> errorMsgOrSupplier, AnySupplier<T> task) throws Exception {
		return doRetry(maxRetries, errorMsgOrSupplier, task, msgWarnChannel);
	}

	protected static <T> T doRetryWithDelay(final int[] delayMsItems, @Nullable Object errorMsgOrSupplier, AnySupplier<T> task, @Nullable MsgWarnChannel channel) throws Exception {
		int errorCount = 0;
		do {
			try {
				return task.get();
			} catch (Exception e) {
				if (errorCount++ < delayMsItems.length) {
					if (errorMsgOrSupplier == null) {
						errorMsgOrSupplier = "第" + errorCount + "次执行任务时出错，正在重试！";
					} else {
						errorMsgOrSupplier = "第" + errorCount + "次" + StringUtil.toString(ObjUtil.tryUnwrap(errorMsgOrSupplier));
					}
					logErrorInternal(log, errorMsgOrSupplier, e, channel);
					int delayInMs = delayMsItems[errorCount - 1];
					if (delayInMs > 0) {
						//noinspection BusyWait
						Thread.sleep(delayInMs);
					}
					continue;
				}
				throw e;
			}
		} while (true);
	}

	/**
	 * 执行指定的任务，如果报错，则基于指定的延迟时间数组依次进行重试
	 *
	 * @param delayMsItems 依次重试的时间间隔数组（单位：毫秒）
	 * @param channel 用于上报告警通知的渠道，可以为 null（表示不上报）
	 */
	public static <T> T retryWithDelay(final int[] delayMsItems, @Nullable CharSequence errorMsgOrSupplier, AnySupplier<T> task, @Nullable MsgWarnChannel channel) throws Exception {
		return doRetryWithDelay(delayMsItems, errorMsgOrSupplier, task, channel);
	}

	/**
	 * 执行指定的任务，如果报错，则基于指定的延迟时间数组依次进行重试
	 *
	 * @param delayMsItems 依次重试的时间间隔数组（单位：毫秒）
	 * @param channel 用于上报告警通知的渠道，可以为 null（表示不上报）
	 */
	public static <T> T retryWithDelay(final int[] delayMsItems, @Nullable Supplier<String> errorMsgOrSupplier, AnySupplier<T> task, @Nullable MsgWarnChannel channel) throws Exception {
		return doRetryWithDelay(delayMsItems, errorMsgOrSupplier, task, channel);
	}

	/**
	 * 执行指定的任务，如果报错，则基于指定的延迟时间数组依次进行重试
	 *
	 * @param delayMsItems 依次重试的时间间隔数组（单位：毫秒）
	 */
	public static <T> T retryWithDelay(final int[] delayMsItems, @Nullable CharSequence errorMsgOrSupplier, AnySupplier<T> task) throws Exception {
		return doRetryWithDelay(delayMsItems, errorMsgOrSupplier, task, msgWarnChannel);
	}

	/**
	 * 执行指定的任务，如果报错，则基于指定的延迟时间数组依次进行重试
	 *
	 * @param delayMsItems 依次重试的时间间隔数组（单位：毫秒）
	 */
	public static <T> T retryWithDelay(final int[] delayMsItems, @Nullable Supplier<String> errorMsgOrSupplier, AnySupplier<T> task) throws Exception {
		return doRetryWithDelay(delayMsItems, errorMsgOrSupplier, task, msgWarnChannel);
	}

}