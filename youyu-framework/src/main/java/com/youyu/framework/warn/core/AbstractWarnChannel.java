package com.youyu.framework.warn.core;

import com.youyu.framework.context.Env;
import com.youyu.common.model.Result;
import com.youyu.framework.warn.config.WarnProperties;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.function.Function;

/**
 * 告警通道抽象基类
 * <p>
 * 职责：
 * 1. 提供通用的告警发送逻辑
 * 2. 处理环境判断（local禁用、去重检查）
 * 3. 使用JDK HttpClient发送HTTP请求
 * 4. 定义模板方法供子类实现平台特定逻辑
 */
@Slf4j
public abstract class AbstractWarnChannel implements MsgWarnChannel {

    /** HTTP客户端（线程安全，可复用） */
    protected static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /** 配置属性 */
    protected final WarnProperties properties;
    /** 去重管理器 */
    @Nullable
    protected final WarnDistinctManager distinctManager;

    public AbstractWarnChannel(WarnProperties properties, @Nullable WarnDistinctManager distinctManager) {
        this.properties = properties;
        this.distinctManager = distinctManager;
    }

    /**
     * 获取平台类型
     */
    protected abstract WarnPlatform getPlatform();

    /**
     * 构建签名URL（钉钉需要，企业微信不需要）
     *
     * @param baseUrl 基础URL
     * @return 签名后的URL
     */
    protected abstract String buildSignedUrl(String baseUrl);

    /**
     * 构建平台特定的消息payload
     *
     * @param content 消息内容
     * @param atAll   是否@所有人
     * @return JSON格式的消息payload
     */
    protected abstract String buildPlatformPayload(String content, boolean atAll);

    /**
     * 构建Markdown消息payload
     *
     * @param title   标题
     * @param markdown Markdown内容
     * @param atAll   是否@所有人
     * @return JSON格式的消息payload
     */
    protected abstract String buildMarkdownPayload(String title, String markdown, boolean atAll);

    @Override
    public Result<String> sendBugMsg(@Nullable String uri, Throwable e, boolean atAll) {
        return doSend(v -> {
            String content = WarnMessageBuilder.buildExceptionContent(uri, e);
            return buildPlatformPayload(content, atAll);
        }, true);
    }

    @Override
    public Result<String> sendBugMsg(String bugMsg, boolean atAll) {
        return doSend(v -> {
            String content = WarnMessageBuilder.buildBusinessExceptionContent(bugMsg);
            return buildPlatformPayload(content, atAll);
        }, true);
    }

    @Override
    public Result<String> sendMsg(String msg, boolean atAll) {
        return doSend(v -> {
            String content = WarnMessageBuilder.buildNotificationContent(msg);
            return buildPlatformPayload(content, atAll);
        }, false);
    }

    @Override
    public Result<String> sendMarkdownMsg(String title, String markdown, boolean atAll) {
        return doSend(v -> 
            buildMarkdownPayload(title, markdown, atAll), 
            true
        );
    }

    // ==================== 异步方法（先判断再开虚拟线程）====================

    @Override
    public void sendBugMsgAsync(@Nullable String uri, Throwable e, boolean atAll) {
        doSend(v -> {
            String content = WarnMessageBuilder.buildExceptionContent(uri, e);
            return buildPlatformPayload(content, atAll);
        }, true, true);
    }

    @Override
    public void sendBugMsgAsync(String bugMsg, boolean atAll) {
        doSend(v -> {
            String content = WarnMessageBuilder.buildBusinessExceptionContent(bugMsg);
            return buildPlatformPayload(content, atAll);
        }, true, true);
    }

    @Override
    public void sendMsgAsync(String msg, boolean atAll) {
        doSend(v -> {
            String content = WarnMessageBuilder.buildNotificationContent(msg);
            return buildPlatformPayload(content, atAll);
        }, false, true);
    }

    @Override
    public void sendMarkdownMsgAsync(String title, String markdown, boolean atAll) {
        doSend(v -> 
            buildMarkdownPayload(title, markdown, atAll), 
            true, true
        );
    }

    /**
     * 核心发送逻辑
     *
     * @param payloadBuilder payload构建器
     * @param distinct       是否启用去重
     * @return 发送结果
     */
    protected Result<String> doSend(Function<Void, String> payloadBuilder, boolean distinct) {
        // 1. 检查是否启用
        if (!isEnabled()) {
            log.debug("告警功能已禁用（当前环境：{}）", Env.CURRENT.getValue());
            return Result.success();
        }

        // 2. 构建消息payload
        String payload = payloadBuilder.apply(null);
        log.debug("准备发送告警消息：{}", payload);

        // 3. 去重检查
        if (distinct && !canSend(payload)) {
            log.debug("消息在一分钟内已发送过，跳过");
            return Result.<String>success().setMessage("已经在一分钟内发过");
        }

        // 4. 发送HTTP请求
        return sendHttpRequest(payload);
    }

    protected Result<String> doSend(Function<Void, String> payloadBuilder, boolean distinct, boolean async) {
        // 1. 检查是否启用
        if (!isEnabled()) {
            log.debug("告警功能已禁用（当前环境：{}）", Env.CURRENT.getValue());
            return Result.success();
        }

        // 2. 构建消息payload
        String payload = payloadBuilder.apply(null);
        log.debug("准备发送告警消息：{}", payload);

        // 3. 去重检查
        if (distinct && !canSend(payload)) {
            log.debug("消息在一分钟内已发送过，跳过");
            return Result.<String>success().setMessage("已经在一分钟内发过");
        }

        // 4. 发送HTTP请求
        if (async) {
            Thread.startVirtualThread(() -> sendHttpRequest(payload));
            return Result.success();
        }else {
            return sendHttpRequest(payload);
        }
    }

    /**
     * 发送HTTP请求
     *
     * @param payload 消息payload
     * @return 发送结果
     */
    protected Result<String> sendHttpRequest(String payload) {
        try {
            String url = buildSignedUrl(properties.getUrl());
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            String result = response.body();

            if (code == 200) {
                log.debug("告警消息发送成功：{}", result);
                return Result.success(result);
            } else {
                log.warn("告警消息发送失败，状态码：{}，响应：{}", code, result);
                return Result.<String>error(String.valueOf(code), "发送异常！").setData(result);
            }
        } catch (Exception e) {
            log.error("告警消息发送异常", e);
            return Result.error(null);
        }
    }

    /**
     * 检查是否可以发送（去重逻辑）
     */
    protected boolean canSend(String payload) {
        if (distinctManager == null) {
            return true;
        }
        // 使用MD5缩短key长度
        String msgKey = md5(payload);
        return distinctManager.canSend(msgKey);
    }

    /**
     * 计算MD5
     */
    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            // 如果MD5失败，使用原始字符串
            return input;
        }
    }

    /**
     * 判断告警功能是否启用
     * <p>
     * local环境自动禁用
     */
    protected boolean isEnabled() {
        return !Env.inLocal() && properties.isEnabled();
    }
}
