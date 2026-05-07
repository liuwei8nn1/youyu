package com.youyu.framework.warn.platform;

import com.alibaba.fastjson2.JSONObject;
import com.youyu.framework.warn.config.WarnProperties;
import com.youyu.framework.warn.core.*;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * 钉钉告警通道实现
 * <p>
 * 钉钉机器人特点：
 * 1. 需要签名验证（timestamp + sign）
 * 2. @所有人使用独立的at.isAtAll字段
 * 3. 消息类型字段为msgtype
 */
@Slf4j
public class DingTalkWarnChannel extends AbstractWarnChannel {

    public DingTalkWarnChannel(WarnProperties properties, @Nullable WarnDistinctManager distinctManager) {
        super(properties, distinctManager);
    }

    @Override
    protected WarnPlatform getPlatform() {
        return WarnPlatform.DINGTALK;
    }

    @Override
    protected String buildSignedUrl(String baseUrl) {
        String secret = properties.getSecret();
        if (secret == null || secret.isEmpty()) {
            log.warn("钉钉机器人必须配置密钥，当前未配置");
            return baseUrl;
        }

        // 钉钉签名算法：timestamp + \n + secret -> HmacSHA256 -> Base64 -> URLEncode
        try {
            long timestamp = System.currentTimeMillis();
            String source = timestamp + "\n" + secret;
            
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(source.getBytes(StandardCharsets.UTF_8));
            String sign = URLEncoder.encode(
                new String(Base64.getEncoder().encode(signData)), 
                StandardCharsets.UTF_8
            );
            
            return baseUrl + "&timestamp=" + timestamp + "&sign=" + sign;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("钉钉签名计算失败", e);
            return baseUrl;
        }
    }

    @Override
    protected String buildPlatformPayload(String content, boolean atAll) {
        JSONObject payload = new JSONObject();
        payload.put("msgtype", "text");
        
        JSONObject textObj = new JSONObject();
        textObj.put("content", content);
        payload.put("text", textObj);
        
        // 钉钉@所有人：使用独立的at字段
        if (atAll) {
            JSONObject atObj = new JSONObject();
            atObj.put("isAtAll", true);
            payload.put("at", atObj);
        }
        
        return payload.toJSONString();
    }

    @Override
    protected String buildMarkdownPayload(String title, String markdown, boolean atAll) {
        JSONObject payload = new JSONObject();
        payload.put("msgtype", "markdown");
        
        JSONObject markdownObj = new JSONObject();
        markdownObj.put("title", title);
        markdownObj.put("text", markdown);  // 注意：钉钉用text而不是content
        payload.put("markdown", markdownObj);
        
        // 钉钉@所有人：使用独立的at字段
        if (atAll) {
            JSONObject atObj = new JSONObject();
            atObj.put("isAtAll", true);
            payload.put("at", atObj);
        }
        
        return payload.toJSONString();
    }
}
