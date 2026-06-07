package com.community.idle.common;

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

    public static class CurrentUser {
        private Long userId;
        private String username;
        private Integer role;

        public CurrentUser() {
        }

        public CurrentUser(Long userId, String username, Integer role) {
            this.userId = userId;
            this.username = username;
            this.role = role;
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

        public Integer getRole() {
            return role;
        }

        public void setRole(Integer role) {
            this.role = role;
        }
    }
}
