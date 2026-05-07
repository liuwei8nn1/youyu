package com.youyu.auth.sdk.aspect;

import java.lang.reflect.Method;
import java.util.Objects;

import com.youyu.auth.api.model.*;
import com.youyu.auth.sdk.AuthServiceClient;
import com.youyu.common.constant.BaseI18nKey;
import com.youyu.framework.context.*;
import com.youyu.framework.context.web.util.RequestContextUtil;
import com.youyu.common.exception.PermissionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * 权限检查切面
 * <p>
 * 自动拦截带有 @Permission 注解的方法，进行权限校验
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionCheckAspect {

    private final AuthServiceClient authServiceClient;

    /**
     * 在方法执行前检查权限
     *
     * @param joinPoint 连接点
     */
    @Before("@annotation(com.youyu.auth.api.model.Permission)")
    public void checkPermission(JoinPoint joinPoint) {
        // 1. 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 2. 获取 @Permission 注解
        Permission permission = method.getAnnotation(Permission.class);
        if (permission == null || Objects.equals(permission.value(), Permission.NONE)) {
            return;
        }

        // 3. 获取当前用户信息
        UserInfo userInfo = RequestContextUtil.getCurrentUserInfo();
        if (userInfo.isEmpty()) {
            log.warn("用户未登录，拒绝访问: {}.{}", method.getDeclaringClass().getSimpleName(), method.getName());
            throw new PermissionException(I18N.msg(BaseI18nKey.NOT_LOGGED_IN));
        }

        // 4. 解析最终权限码（从 request.getParameter 获取）
        String permissionCode = PermissionCodeResolver.resolve(permission);
        UserType userType = permission.userType();

        log.debug("开始权限检查: userId={}, userType={}, permissionCode={}",
                userInfo.getUserId(), userInfo.getUserType(), permissionCode);

        // 5. 检查是否有任一权限码的权限
        if (!authServiceClient.hasPermission(
                userInfo.getUserId(),
                userInfo.getUserType(),
                permissionCode,
                userType
        )) {
            log.warn("权限不足: userId={}, userType={}, permissionCode={}, method={}.{}",
                    userInfo.getUserId(), userInfo.getUserType(), permissionCode,
                    method.getDeclaringClass().getSimpleName(), method.getName());
            throw new PermissionException(I18N.msg(BaseI18nKey.PERMISSION_DENIED));

        } else {
            log.debug("权限检查通过: userId={}, permissionCode={}", userInfo.getUserId(), permissionCode);
        }
    }
}
