package com.youyu.framework.warn.listener;

import com.youyu.framework.context.Env;
import com.youyu.framework.warn.core.MsgWarnChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

/**
 * 应用启动监听器
 * <p>
 * 职责：应用启动成功后发送通知告警
 * <p>
 * 注意：此类通过WarnListenerConfiguration注册为Bean，不使用@Component
 */
@Slf4j
public class ApplicationStartedListener implements ApplicationListener<ApplicationReadyEvent> {

    MsgWarnChannel msgWarnChannel;

    public ApplicationStartedListener(MsgWarnChannel msgWarnChannel) {
        this.msgWarnChannel = msgWarnChannel;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String msg = "===========>>>>>>> 应用启动成功 [" + Env.CURRENT.getLabel() + "]";
        log.info(msg);
        // 使用虚拟线程异步发送告警，不阻塞应用启动流程
        if (msgWarnChannel != null) {
            Thread.startVirtualThread(() -> {
                try {
                    msgWarnChannel.sendMsg(msg, true);
                } catch (Exception e) {
                    log.warn("发送启动通知失败", e);
                }
            });
        }
    }

}
