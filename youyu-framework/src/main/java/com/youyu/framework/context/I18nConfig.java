package com.youyu.framework.context;

import com.youyu.common.constant.BaseI18nKey;
import com.youyu.common.model.Result;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;

@Configuration
public class I18nConfig implements InitializingBean {

    @Resource
    MessageSource messageSource;

    @Override
    public void afterPropertiesSet() throws Exception {
        I18N.messageSource = messageSource;
        Result.successMsgSupplier = () -> I18N.msg(BaseI18nKey.MESSAGE_SUCCESS);
    }

}
