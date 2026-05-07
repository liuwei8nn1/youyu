package com.youyu.framework.warn.platform;

import com.alibaba.fastjson2.JSONObject;
import com.youyu.framework.warn.config.WarnProperties;
import com.youyu.framework.warn.core.*;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

/**
 * 企业微信告警通道实现
 * <p>
 * 企业微信机器人特点：
 * 1. 不需要签名（除非启用密钥验证）
 * 2. @所有人使用mentioned_list字段，在content对象内
 * 3. 消息类型字段为msgtype
 */
@Slf4j
public class WechatWarnChannel extends AbstractWarnChannel {

    public WechatWarnChannel(WarnProperties properties, @Nullable WarnDistinctManager distinctManager) {
        super(properties, distinctManager);
    }

    @Override
    protected WarnPlatform getPlatform() {
        return WarnPlatform.WECHAT;
    }

    @Override
    protected String buildSignedUrl(String baseUrl) {
        // 企业微信可选密钥验证
        String secret = properties.getSecret();
        if (secret != null && !secret.isEmpty()) {
            log.warn("企业微信密钥验证暂未实现，将使用无密钥方式发送");
        }
        // 企业微信webhook URL不需要额外签名
        return baseUrl;
    }

    @Override
    protected String buildPlatformPayload(String content, boolean atAll) {
        JSONObject payload = new JSONObject();
        payload.put("msgtype", "text");
        
        JSONObject textObj = new JSONObject();
        textObj.put("content", content);
        
        // 企业微信@所有人：在text对象内添加mentioned_list
        if (atAll) {
            textObj.put("mentioned_list", new String[]{"@all"});
        }
        
        payload.put("text", textObj);
        return payload.toJSONString();
    }

    @Override
    protected String buildMarkdownPayload(String title, String markdown, boolean atAll) {
        JSONObject payload = new JSONObject();
        payload.put("msgtype", "markdown");
        
        JSONObject markdownObj = new JSONObject();
        markdownObj.put("title", title);
        markdownObj.put("content", markdown);
        
        // 企业微信markdown不支持@所有人，忽略atAll参数
        if (atAll) {
            log.debug("企业微信markdown消息不支持@所有人");
        }
        
        payload.put("markdown", markdownObj);
        return payload.toJSONString();
    }
}
