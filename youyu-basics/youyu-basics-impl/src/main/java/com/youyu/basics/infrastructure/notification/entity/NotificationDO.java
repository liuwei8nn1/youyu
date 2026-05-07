package com.youyu.basics.infrastructure.notification.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youyu.basics.api.notification.enums.NotificationType;
import com.youyu.framework.datasource.mybatis.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 通知数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification")
public class NotificationDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 通知类型
     */
    private NotificationType type;

    /**
     * 接收者
     */
    private String receiver;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 发送状态（PENDING/SENT/FAILED）
     */
    private String status;
}
