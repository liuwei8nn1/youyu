package com.youyu.framework.context.web.handler;

import java.util.List;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.youyu.framework.context.I18N;
import com.youyu.common.constant.BaseI18nKey;
import com.youyu.framework.context.*;
import com.youyu.common.model.Result;
import com.youyu.common.exception.AuthFailException;
import com.youyu.common.exception.DomainException;
import com.youyu.common.exception.PermissionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BlockException.class)
    public Result<Object> handleBlockException(BlockException ex) {
        String message = I18N.msg(getBlockExceptionKey(ex));
        UserInfo currentUserInfo = UserContextHolder.getUserInfo();
        return Result.error(Result.TOO_MANY_REQUESTS, message).setTraceId(currentUserInfo.getTraceId());
    }

    @ExceptionHandler(FlowException.class)
    public Result<Object> handleFlowException(FlowException ex) {
        String message = I18N.msg(BaseI18nKey.SENTINEL_FLOW_CONTROL);
        UserInfo currentUserInfo = UserContextHolder.getUserInfo();
        return Result.error(Result.TOO_MANY_REQUESTS, message).setTraceId(currentUserInfo.getTraceId());
    }

    @ExceptionHandler(DegradeException.class)
    public Result<Object> handleDegradeException(DegradeException ex) {
        String message = I18N.msg(BaseI18nKey.SENTINEL_DEGRADE);
        UserInfo currentUserInfo = UserContextHolder.getUserInfo();
        return Result.error(Result.TOO_MANY_REQUESTS, message).setTraceId(currentUserInfo.getTraceId());
    }

    @ExceptionHandler(ParamFlowException.class)
    public Result<Object> handleParamFlowException(ParamFlowException ex) {
        String message = I18N.msg(BaseI18nKey.SENTINEL_PARAM_FLOW);
        UserInfo currentUserInfo = UserContextHolder.getUserInfo();
        return Result.error(Result.TOO_MANY_REQUESTS, message).setTraceId(currentUserInfo.getTraceId());
    }

    @ExceptionHandler(SystemBlockException.class)
    public Result<Object> handleSystemBlockException(SystemBlockException ex) {
        String message = I18N.msg(BaseI18nKey.SENTINEL_SYSTEM_BLOCK);
        UserInfo currentUserInfo = UserContextHolder.getUserInfo();
        return Result.error(Result.TOO_MANY_REQUESTS, message).setTraceId(currentUserInfo.getTraceId());
    }

    @ExceptionHandler(AuthorityException.class)
    public Result<Object> handleAuthorityException(AuthorityException ex) {
        String message = I18N.msg(BaseI18nKey.SENTINEL_AUTHORITY_DENIED);
        UserInfo currentUserInfo = UserContextHolder.getUserInfo();
        return Result.error(String.valueOf(HttpStatus.FORBIDDEN.value()), message).setTraceId(currentUserInfo.getTraceId());
    }

    @ExceptionHandler(DomainException.class)
    public Result<Object> handleDomainException(DomainException ex) {
        UserInfo currentUserInfo = UserContextHolder.getUserInfo();
        log.warn("领域异常: {}", ex.getMessage());
        return Result.error(ex.getCode(), ex.getMessage()).setTraceId(currentUserInfo.getTraceId());
    }

    @ExceptionHandler(Exception.class)
    public Result<Object> handleGenericException(Exception ex) {
        UserInfo currentUserInfo = UserContextHolder.getUserInfo();
        log.error("业务异常：", ex);
        if( ex instanceof MethodArgumentNotValidException e){
            List<ObjectError> allErrors = e.getAllErrors();
            StringBuilder sb = new StringBuilder();
            for (ObjectError allError : allErrors) {
                String i18nKey = allError.getDefaultMessage();
                if(sb.isEmpty()) {
	                sb.append(I18N.msg(i18nKey));
                }else {
                    sb.append("\r\n").append(I18N.msg(i18nKey));
                }
            }
            return Result.error(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), sb.toString()).setTraceId(currentUserInfo.getTraceId());
        }else if( ex instanceof AuthFailException e){
            return Result.error(e.getCode(), e.getMessage()).setTraceId(currentUserInfo.getTraceId());
        }else if( ex instanceof PermissionException e){
            return Result.error(e.getCode(), e.getMessage()).setTraceId(currentUserInfo.getTraceId());
        }
        String message = I18N.msg(BaseI18nKey.COMMON_SERVER_ERROR, ex.getMessage());
        return Result.error(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), message).setTraceId(currentUserInfo.getTraceId());
    }

    /**
     * 根据BlockException类型获取对应的国际化key
     */
    private String getBlockExceptionKey(BlockException ex) {
        if (ex instanceof FlowException) {
            return BaseI18nKey.SENTINEL_FLOW_CONTROL;
        } else if (ex instanceof DegradeException) {
            return BaseI18nKey.SENTINEL_DEGRADE;
        } else if (ex instanceof ParamFlowException) {
            return BaseI18nKey.SENTINEL_PARAM_FLOW;
        } else if (ex instanceof SystemBlockException) {
            return BaseI18nKey.SENTINEL_SYSTEM_BLOCK;
        } else if (ex instanceof AuthorityException) {
            return BaseI18nKey.SENTINEL_AUTHORITY_DENIED;
        }
        return BaseI18nKey.COMMON_SERVER_ERROR;
    }
}
