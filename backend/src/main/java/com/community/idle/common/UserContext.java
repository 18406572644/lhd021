package com.community.idle.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public static Integer getRole() {
        CurrentUser user = HOLDER.get();
        return user != null ? user.getRole() : null;
    }

    public static boolean isAdmin() {
        CurrentUser user = HOLDER.get();
        return user != null && user.getRole() == 1;
    }

    public static void remove() {
        HOLDER.remove();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentUser {
        private Long userId;
        private String username;
        private Integer role;
    }
}
