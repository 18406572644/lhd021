package com.community.idle.common;

import java.util.*;

public class UserContext {

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        CurrentUser user = HOLDER.get();
        return user != null ? user.getUserId() : null;
    }

    public static String getUsername() {
        CurrentUser user = HOLDER.get();
        return user != null ? user.getUsername() : null;
    }

    public static Long getDeptId() {
        CurrentUser user = HOLDER.get();
        return user != null ? user.getDeptId() : null;
    }

    public static boolean isAdmin() {
        CurrentUser user = HOLDER.get();
        return user != null && user.getRoleCodes() != null && user.getRoleCodes().contains("SUPER_ADMIN");
    }

    public static Set<String> getRoleCodes() {
        CurrentUser user = HOLDER.get();
        return user != null && user.getRoleCodes() != null ? user.getRoleCodes() : Collections.emptySet();
    }

    public static boolean hasRole(String roleCode) {
        return getRoleCodes().contains(roleCode);
    }

    public static boolean hasAnyRole(String... roleCodes) {
        Set<String> userRoles = getRoleCodes();
        for (String roleCode : roleCodes) {
            if (userRoles.contains(roleCode)) {
                return true;
            }
        }
        return false;
    }

    public static void remove() {
        HOLDER.remove();
    }

    public static class CurrentUser {
        private Long userId;
        private String username;
        private Long deptId;
        private Set<String> roleCodes;
        private Set<String> permissions;

        public CurrentUser() {
        }

        public CurrentUser(Long userId, String username, Long deptId, Set<String> roleCodes) {
            this.userId = userId;
            this.username = username;
            this.deptId = deptId;
            this.roleCodes = roleCodes;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public Long getDeptId() {
            return deptId;
        }

        public void setDeptId(Long deptId) {
            this.deptId = deptId;
        }

        public Set<String> getRoleCodes() {
            return roleCodes;
        }

        public void setRoleCodes(Set<String> roleCodes) {
            this.roleCodes = roleCodes;
        }

        public Set<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(Set<String> permissions) {
            this.permissions = permissions;
        }
    }
}
