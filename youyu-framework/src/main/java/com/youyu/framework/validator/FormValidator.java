package com.youyu.framework.validator;

import java.beans.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.youyu.common.util.ObjUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.reflect.FieldUtils;

@Slf4j
public class FormValidator implements ConstraintValidator<CheckForm, Object> {

	protected CheckForm check;

	private static Predicate<String> anonymousFieldMatcher = FormValidator::isAnonymousField;

	/**
	 * 设置匿名字段匹配器
	 *
	 * @param mater 字段名匹配器，用于判断字段是否应该被跳过
	 */
	public static void setAnonymousFieldMatcher(Predicate<String> mater) {
		anonymousFieldMatcher = Objects.requireNonNull(mater);
	}

	/**
	 * 判断指定字段是否应该跳过日志记录
	 *
	 * @param fieldName 要检查的字段名
	 * @return 如果字段应该跳过则返回 true，否则返回 false
	 */
	public static boolean shouldSkip(String fieldName) {
		return anonymousFieldMatcher.test(fieldName);
	}

	/**
	 * 判断指定名称是否为匿名字段
	 *
	 * @param name 要检查的字段名称
	 * @return 如果是匿名字段（密码相关）则返回 true，否则返回 false
	 */
	public static boolean isAnonymousField(final String name) {
		return "password".equals(name) || name.endsWith("Pwd");
	}

	public void initialize(CheckForm constraint) {
		check = constraint;
	}

	public boolean isValid(Object obj, ConstraintValidatorContext context) {
		context.disableDefaultConstraintViolation();
		if (obj == null) {
			if (check.notNull()) {
				String error = ValidateError.resolveRequiredError(check.label());
				context.buildConstraintViolationWithTemplate(error).addConstraintViolation();
				return false;
				// throw new ValidateException(error);
			}
			return true;
		}
		return validate(obj, check, context);
	}

	public static boolean validate(Object obj, CheckForm check, ConstraintValidatorContext context) {
		Form form = null;
		if (obj instanceof Form) {
			form = (Form) obj;
			form.preHandle();
		}
		BeanInfo beanInfo;
		try {
			beanInfo = BeanInfo.of(obj.getClass(), check);
		} catch (Exception e) {
			throw new IllegalStateException("初始化校验器时出错！", e);
		}
		ValidateContext.ValidateItem root = new ValidateContext.ValidateItem(beanInfo.root, obj);
		Map<String, ValidateContext.ValidateItem> items = new LinkedHashMap<>(beanInfo.items.size(), 1F);
		for (Entry<String, ValidateContext.ValidateItemInfo> entry : beanInfo.items.entrySet()) {
			items.put(entry.getKey(), new ValidateContext.ValidateItem(entry.getValue()));
		}
		ValidateContext.DefaultValidateContext ctx = new ValidateContext.DefaultValidateContext(root, items);
		boolean stop = false;
		final boolean debugEnabled = log.isDebugEnabled();
		for (Entry<String, ValidateContext.ValidateItem> entry : items.entrySet()) {
			ValidateContext.ValidateItem item = entry.getValue();
			ctx.setCurrent(item);
			Object val = item.getFieldValue(obj);
			if (debugEnabled) {
				String name = item.getInfo().getName();
				// 如果是敏感字段（比如 密码、证件号码 等），在 DEBUG 级别 默认匿名化处理，显示为 <***>，只有在 TRACE 级别才显示完整数据
				if (anonymousFieldMatcher.test(name)) {
					if (log.isTraceEnabled()) {
						log.trace("开始校验：【{}】 = {}", name, val);
					} else {
						log.debug("开始校验：【{}】 = <***>", name);
					}
				} else {
					log.debug("开始校验：【{}】 = {}", name, val);
				}
			}
			if (val == null || (val instanceof CharSequence && val.toString().isEmpty())) {
				if (item.getInfo().required) {
					ctx.addError(() -> ValidateError.resolveRequiredError(ctx.getCurrentLabel()));
					if (debugEnabled) {
						log.debug("\t\t{} 执行校验结果：{}", "@Check.required", RuleValidator.Result.NO);
					}
					stop = true;
				}
			} else {
				for (ValidateContext.AnnotatedValidatorEntry v : item.getInfo().getToValidates()) {
					ctx.setCurrentEntry(v);
					if (debugEnabled) {
						log.debug("\t\t开始执行校验规则：{}", v.getRule().annotationType().getSimpleName());
					}
					RuleValidator<?, Object> validator = v.getValidator();
					RuleValidator.Result result = validator.validate(item.val, ctx);
					if (debugEnabled) {
						log.debug("\t\t{} 执行校验结果：{}", v.getRule().annotationType().getSimpleName(), result);
					}
					if (result != RuleValidator.Result.YES) {
						if (result == RuleValidator.Result.NO) {
							stop = true;
						}
						break;
					}
				}
			}
			if (stop) {
				break;
			}
		}
		List<Supplier<String>> errors = ctx.getErrors();
		if (ObjUtil.isValid(errors)) {
			context.buildConstraintViolationWithTemplate(errors.get(0).get()).addConstraintViolation();
			return false;
			// throw new ValidateException(errors.get(0).get());
		}
		if (form != null) {
			form.validate();
			form.postHandle();
		}
		return true;
	}

	public static class BeanInfo {

		public final ValidateContext.ValidateItemInfo root;
		public final Map<String, ValidateContext.ValidateItemInfo> items;

		static Map<Class<?>, BeanInfo> cache = new ConcurrentHashMap<>();

		public BeanInfo(ValidateContext.ValidateItemInfo root, Map<String, ValidateContext.ValidateItemInfo> items) {
			this.root = root;
			this.items = items;
		}

		public static BeanInfo of(Class<?> clazz, CheckForm check) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IntrospectionException {
			BeanInfo info = cache.get(clazz);
			if (info == null) {
				info = buildBeanInfo(clazz, check);
				cache.put(clazz, info);
			}
			return info;
		}

		protected static BeanInfo buildBeanInfo(Class<?> clazz, CheckForm check) throws IntrospectionException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			ValidateContext.ValidateItemInfo rootInfo = new ValidateContext.ValidateItemInfo(clazz, check.label(), check.notNull());
			Field[] fields = FieldUtils.getFieldsWithAnnotation(clazz, Check.class);
			final PropertyDescriptor[] pds = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
			final Map<String, Method> fieldGetters = new LinkedHashMap<>(pds.length, 1F);
			for (PropertyDescriptor pd : pds) {
				if ("class".equals(pd.getName())) {
					continue;
				}
				Method getter = pd.getReadMethod();
				if (getter != null) {
					Check label = getter.getAnnotation(Check.class);
					if (label != null) {
						fieldGetters.put(pd.getName(), getter);
					}
				}
			}
			Map<String, ValidateContext.ValidateItemInfo> items = new LinkedHashMap<>(fields.length, 1F);
			final List<ValidateContext.AnnotatedValidatorEntry> list = new ArrayList<>(5);
			for (Field field : fields) {
				Annotation[] annotations = field.getAnnotations();
				Check fieldCheck = field.getAnnotation(Check.class);
				extractRules(list, annotations);
				String name = field.getName();
				Method method = fieldGetters.remove(name);
				if (method != null) {
					fieldCheck = method.getAnnotation(Check.class);
					extractRules(list, method.getAnnotations());
				}
				Collections.sort(list);
				items.put(name, new ValidateContext.ValidateItemInfo(field, fieldCheck.value(), fieldCheck.required(), list.toArray(new ValidateContext.AnnotatedValidatorEntry[0])));
				list.clear();
			}
			return new BeanInfo(rootInfo, items);
		}

		protected static void extractRules(List<ValidateContext.AnnotatedValidatorEntry> list, Annotation[] annotations) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			for (Annotation a : annotations) {
				Rule rule = a.annotationType().getAnnotation(Rule.class);
				if (rule != null) {
					Class<? extends RuleValidator<Annotation, Object>>[] vClasses = ObjUtil.castType(rule.value());
					for (Class<? extends RuleValidator<Annotation, Object>> vClass : vClasses) {
						RuleValidator<Annotation, Object> validator = vClass.getConstructor().newInstance();
						list.add(new ValidateContext.AnnotatedValidatorEntry(a, validator));
					}
				}
			}
		}

	}

}