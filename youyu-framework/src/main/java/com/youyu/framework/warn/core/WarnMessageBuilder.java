package com.youyu.framework.warn.core;

import com.youyu.framework.context.Env;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 告警消息内容构建器
 * <p>
 * 职责：
 * 1. 构建异常信息内容（根据环境决定是否包含完整堆栈）
 * 2. 构建业务异常内容
 * 3. 构建普通通知内容
 * <p>
 * 注意：平台特定的JSON格式构建已移至各Channel类中
 */
public class WarnMessageBuilder {

    /**
     * 构建异常告警内容
     *
     * @param uri      请求URI
     * @param e        异常对象
     * @return 格式化的异常信息
     */
    public static String buildExceptionContent(@Nullable String uri, Throwable e) {
        if (uri == null) {
            uri = "";
        }

        // 根据环境决定是否包含完整堆栈
        if (shouldIncludeFullStackTrace()) {
            StringWriter out = new StringWriter(2048);
            out.write("系统异常：【" + getAppInfo() + "】=> " + uri + '\n');
            PrintWriter pw = new PrintWriter(out);
            e.printStackTrace(pw);
            return out.toString();
        }

        // 简化模式：只显示关键信息
        StackTraceElement element = getRelevantStackTrace(e);
        if (element == null) {
            return "系统异常：【" + getAppInfo() + "】=> " + uri + '\n' + e.getMessage();
        }
        return "系统异常：【" + getAppInfo() + "】=> " + uri + '\n' +
                e.getMessage() + '\n' +
                element.getClassName() + "." + element.getMethodName() + " ( " + element.getLineNumber() + " )";
    }

    /**
     * 构建业务异常内容
     *
     * @param bugMsg 业务异常消息
     * @return 格式化的业务异常信息
     */
    public static String buildBusinessExceptionContent(String bugMsg) {
        return "业务异常：\n" + getAppInfo() + ":\n" + bugMsg;
    }

    /**
     * 构建普通通知内容
     *
     * @param msg 通知消息
     * @return 格式化的通知信息
     */
    public static String buildNotificationContent(String msg) {
        return "通知信息：\n【" + getAppInfo() + "】\n" + msg;
    }

    /**
     * 获取应用信息（应用名-环境）
     */
    private static String getAppInfo() {
        return Env.getAppName() + "-" + Env.CURRENT.getValue();
        // return "系统异常：【" + Env.getAppName() + "-" +  Env.CURRENT.getValue() + "】=> ";
    }

    /**
     * 判断是否应该包含完整堆栈信息
     * <p>
     * local环境：不包含（禁用告警）
     * dev/test环境：包含完整堆栈
     * prod/uat环境：简化信息
     */
    private static boolean shouldIncludeFullStackTrace() {
        return Env.inDev() || Env.inTest();
    }

    /**
     * 获取相关的堆栈跟踪元素
     * <p>
     * 优先返回项目包内的堆栈，否则返回第一个堆栈
     */
    @Nullable
    private static StackTraceElement getRelevantStackTrace(Throwable exception) {
        final StackTraceElement[] stackTraces = exception.getStackTrace();
        if (stackTraces.length == 0) {
            return null;
        }

        // TODO: 从配置中获取包名前缀，暂时使用默认值
        String packageName = "com.youyu";
        if (StringUtils.isNotEmpty(packageName)) {
            for (StackTraceElement el : stackTraces) {
                if (el.getClassName().startsWith(packageName)) {
                    return el;
                }
            }
        }
        return stackTraces[0];
    }
}
