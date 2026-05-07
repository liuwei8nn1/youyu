package com.youyu.framework.warn.listener;

import com.youyu.framework.context.Env;
import com.youyu.framework.warn.core.MsgWarnChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

/**
 * 应用关闭监听器
 * <p>
 * 职责：应用关闭前发送通知告警
 * <p>
 * 注意：此类通过WarnListenerConfiguration注册为Bean，不使用@Component
 */
@Slf4j
public class ApplicationClosedListener implements ApplicationListener<ContextClosedEvent> {

    MsgWarnChannel msgWarnChannel;

    public ApplicationClosedListener(MsgWarnChannel msgWarnChannel) {
        this.msgWarnChannel = msgWarnChannel;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        String msg = "===========>>>>>>> 应用正在关闭 [" + Env.CURRENT.getLabel() + "]";
        log.info(msg);
        // 使用虚拟线程异步发送告警
        if (msgWarnChannel != null) {
            Thread.startVirtualThread(() -> {
                try {
                    msgWarnChannel.sendMsg(msg, true);
                } catch (Exception e) {
                    log.warn("发送关闭通知失败", e);
                }
            });
        }
    }

}
