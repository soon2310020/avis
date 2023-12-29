package vn.com.twendie.avis.api.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import vn.com.twendie.avis.api.rest.exception.ForbiddenException;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.locale.config.Translator;
import vn.com.twendie.avis.security.annotation.RequirePermission;
import vn.com.twendie.avis.security.core.model.UserPrincipal;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
public class PermissionFilter {

    @Pointcut("@annotation(vn.com.twendie.avis.security.annotation.RequirePermission)")
    public void requirePermission() {
    }

    @Before(value = "requirePermission()")
    public void beforeInvokeMethod(JoinPoint joinPoint) {
        checkRequiredPermission(joinPoint);
    }

    private void checkRequiredPermission(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        if (method.isAnnotationPresent(RequirePermission.class)) {
            String[] acceptedRoles = method.getAnnotation(RequirePermission.class).acceptedRoles();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userPrincipal.getUser();
            if (!Arrays.asList(acceptedRoles).contains(user.getUserRole().getName())) {
                throw new ForbiddenException("Permission deny")
                        .displayMessage(Translator.toLocale("auth.permission_deny"));
            }
        }
    }
}
