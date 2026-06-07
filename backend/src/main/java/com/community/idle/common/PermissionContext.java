package com.community.idle.common;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionContext {

    private static final ThreadLocal<Set<String>> PERMISSIONS = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> ROLES = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> API_PERMISSIONS = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, Set<Long>>> DATA_SCOPE = new ThreadLocal<>();
    private static final ThreadLocal<Integer> DATA_SCOPE_TYPE = new ThreadLocal<>();

    public static void setPermissions(Set<String> permissions) {
        PERMISSIONS.set(permissions);
    }

    public static Set<String> getPermissions() {
        Set<String> permissions = PERMISSIONS.get();
        return permissions != null ? permissions : Collections.emptySet();
    }

    public static void setRoles(Set<String> roles) {
        ROLES.set(roles);
    }

    public static Set<String> getRoles() {
        Set<String> roles = ROLES.get();
        return roles != null ? roles : Collections.emptySet();
    }

    public static void setApiPermissions(Set<String> apiPermissions) {
        API_PERMISSIONS.set(apiPermissions);
    }

    public static Set<String> getApiPermissions() {
        Set<String> apiPermissions = API_PERMISSIONS.get();
        return apiPermissions != null ? apiPermissions : Collections.emptySet();
    }

    public static void setDataScope(Map<String, Set<Long>> dataScope) {
        DATA_SCOPE.set(dataScope);
    }

    public static Map<String, Set<Long>> getDataScope() {
        Map<String, Set<Long>> dataScope = DATA_SCOPE.get();
        return dataScope != null ? dataScope : Collections.emptyMap();
    }

    public static Set<Long> getDataScopeIds(String businessType) {
        Map<String, Set<Long>> dataScope = DATA_SCOPE.get();
        if (dataScope == null) {
            return Collections.emptySet();
        }
        Set<Long> ids = dataScope.get(businessType);
        return ids != null ? ids : Collections.emptySet();
    }

    public static void setDataScopeType(Integer dataScopeType) {
        DATA_SCOPE_TYPE.set(dataScopeType);
    }

    public static Integer getDataScopeType() {
        Integer type = DATA_SCOPE_TYPE.get();
        return type != null ? type : 4;
    }

    public static boolean hasPermission(String permission) {
        if (hasRole("SUPER_ADMIN")) {
            return true;
        }
        return getPermissions().contains(permission) || getApiPermissions().contains(permission);
    }

    public static boolean hasAnyPermission(String... permissions) {
        if (hasRole("SUPER_ADMIN")) {
            return true;
        }
        Set<String> userPermissions = getPermissions();
        Set<String> userApiPermissions = getApiPermissions();
        for (String permission : permissions) {
            if (userPermissions.contains(permission) || userApiPermissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAllPermission(String... permissions) {
        if (hasRole("SUPER_ADMIN")) {
            return true;
        }
        Set<String> userPermissions = getPermissions();
        Set<String> userApiPermissions = getApiPermissions();
        for (String permission : permissions) {
            if (!userPermissions.contains(permission) && !userApiPermissions.contains(permission)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasRole(String role) {
        if (getRoles().contains("SUPER_ADMIN")) {
            return true;
        }
        return getRoles().contains(role);
    }

    public static boolean hasAnyRole(String... roles) {
        Set<String> userRoles = getRoles();
        for (String role : roles) {
            if (userRoles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    public static void clear() {
        PERMISSIONS.remove();
        ROLES.remove();
        API_PERMISSIONS.remove();
        DATA_SCOPE.remove();
        DATA_SCOPE_TYPE.remove();
    }
}
