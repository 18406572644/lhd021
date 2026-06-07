package com.community.idle.interceptor;

import com.community.idle.common.BusinessException;
import com.community.idle.common.PermissionContext;
import com.community.idle.common.ResultCode;
import com.community.idle.common.UserContext;
import com.community.idle.common.annotation.RequirePermission;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Set;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        if (UserContext.isAdmin()) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequirePermission classAnnotation = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        RequirePermission methodAnnotation = handlerMethod.getMethodAnnotation(RequirePermission.class);

        if (classAnnotation == null && methodAnnotation == null) {
            return true;
        }

        RequirePermission annotation = methodAnnotation != null ? methodAnnotation : classAnnotation;

        if (!checkRoles(annotation)) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        if (!checkPermissions(annotation)) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        return true;
    }

    private boolean checkRoles(RequirePermission annotation) {
        String[] roles = annotation.roles();
        if (roles == null || roles.length == 0) {
            return true;
        }

        Set<String> userRoles = PermissionContext.getRoles();
        RequirePermission.Logical logical = annotation.logical();

        if (logical == RequirePermission.Logical.AND) {
            return userRoles.containsAll(Arrays.asList(roles));
        } else {
            for (String role : roles) {
                if (userRoles.contains(role)) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean checkPermissions(RequirePermission annotation) {
        String[] permissions = annotation.value();
        if (permissions == null || permissions.length == 0) {
            return true;
        }

        Set<String> userPermissions = PermissionContext.getPermissions();
        RequirePermission.Logical logical = annotation.logical();

        if (logical == RequirePermission.Logical.AND) {
            return userPermissions.containsAll(Arrays.asList(permissions));
        } else {
            for (String permission : permissions) {
                if (userPermissions.contains(permission)) {
                    return true;
                }
            }
            return false;
        }
    }
}
