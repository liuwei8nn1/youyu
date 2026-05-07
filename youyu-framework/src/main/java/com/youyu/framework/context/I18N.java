package com.youyu.framework.context;

import java.util.Locale;
import java.util.function.Supplier;

import com.youyu.common.exception.ErrorMessageException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * 国际化工具类
 * 使用方式：I18N.msg("message.code")
 * 底层会自动从HTTP请求头 Accept-Language 解析语言
 */
public class I18N {

	public static MessageSource messageSource;

    public static Supplier<Locale> localeSupplier = LocaleContextHolder::getLocale;
	/**
     * 根据消息代码获取国际化消息
     * 自动从HTTP请求头 Accept-Language 解析当前语言
     * 
     * @param code 消息代码，如 "Message.success"
     * @return 国际化后的消息文本
     */
    public static String msg(String code) {
        return msg(code, new Object[]{});
    }

    /**
     * 根据消息代码获取国际化消息（带参数）
     * 自动从HTTP请求头 Accept-Language 解析当前语言
     * 
     * @param code 消息代码，如 "user.welcome"
     * @param args 消息参数，用于替换占位符 {0}, {1} 等
     * @return 国际化后的消息文本
     */
    public static String msg(String code, Object... args) {
        if (messageSource == null) {
            throw new IllegalStateException("MessageSource not initialized. Please ensure I18NConfig is loaded.");
        }
        
        // 从当前请求上下文获取Locale（基于Accept-Language请求头）
        Locale locale = localeSupplier.get();
        
        // 获取国际化消息，如果找不到则返回code本身
        try {
            return messageSource.getMessage(code, args, locale);
        } catch (Exception e) {
            // 如果找不到对应的消息，返回code本身
            return code;
        }
    }

    /**
     * 根据消息代码获取国际化消息（带默认值）
     * 
     * @param code 消息代码
     * @param defaultMessage 默认消息（当找不到对应code时返回）
     * @return 国际化后的消息文本
     */
    public static String msg(String code, String defaultMessage) {
        if (messageSource == null) {
            return defaultMessage;
        }
        
        Locale locale = localeSupplier.get();
        try {
            return messageSource.getMessage(code, null, defaultMessage, locale);
        } catch (Exception e) {
            return defaultMessage;
        }
    }

    /**
     * 根据消息代码获取国际化消息（带参数和默认值）
     * 
     * @param code 消息代码
     * @param args 消息参数
     * @param defaultMessage 默认消息
     * @return 国际化后的消息文本
     */
    public static String msg(String code, Object[] args, String defaultMessage) {
        if (messageSource == null) {
            return defaultMessage;
        }
        
        Locale locale = localeSupplier.get();
        try {
            return messageSource.getMessage(code, args, defaultMessage, locale);
        } catch (Exception e) {
            return defaultMessage;
        }
    }

    public static void assertTrue(boolean result) {
        if (!result) {
            throw new IllegalArgumentException(); // 默认提示 BaseI18nKey.ILLEGAL_REQUEST
        }
    }

    public static void assertTrue(boolean result, String msgCode) {
        if (!result) {
            throw new IllegalArgumentException(msg(msgCode));
        }
    }

    public static void assertTrue(boolean result, String msgCode, Object... args) {
        if (!result) {
            throw new IllegalArgumentException(msg(msgCode, args));
        }
    }

    public static void assertTrue(boolean result, String msgCode, Object arg) {
        if (!result) {
            throw new IllegalArgumentException(msg(msgCode, arg));
        }
    }

    public static void assertNotNull(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException();
        }
    }

    public static <T> T assertNotNull(T obj, String msgCode) {
        if (obj == null) {
            throw new IllegalArgumentException(msg(msgCode));
        }
        return obj;
    }

    public static <T> T assertNotNull(T obj, String msgCode, Object arg) {
        if (obj == null) {
            throw new IllegalArgumentException(msg(msgCode, arg));
        }
        return obj;
    }

    public static <T> T assertNotNull(T obj, String msgCode, Object... args) {
        if (obj == null) {
            throw new IllegalArgumentException(msg(msgCode, args));
        }
        return obj;
    }

    public static void assertFalse(boolean result, String msgCode) {
        assertTrue(!result, msgCode);
    }

    public static void assertFalse(boolean result, String msgCode, Object arg) {
        assertTrue(!result, msgCode, arg);
    }

    public static void assertFalse(boolean result, String msgCode, Object... args) {
        assertTrue(!result, msgCode, args);
    }

    public static void assertTrueNoTrace(boolean result, String msgCode) {
        if (!result) {
            throw new ErrorMessageException(msg(msgCode), false);
        }
    }

    public static void assertTrueNoTrace(boolean result, String msgCode, Object... args) {
        if (!result) {
            throw new ErrorMessageException(msg(msgCode, args), false);
        }
    }

    public static void assertTrueNoTrace(boolean result, Supplier<String> msgCode) {
        if (!result) {
            throw new ErrorMessageException(msgCode.get(), false);
        }
    }

    public static void assertFalseNoTrace(boolean result, String msgCode) {
        if (result) {
            throw new ErrorMessageException(msg(msgCode), false);
        }
    }

    public static void assertFalseNoTrace(boolean result, String msgCode, Object... args) {
        if (result) {
            throw new ErrorMessageException(msg(msgCode, args), false);
        }
    }

}
