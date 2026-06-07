package com.community.idle.common;

public class StatusConverter {

    public static String getItemStatus(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 1 -> "可互换";
            case 2 -> "互换中";
            case 3 -> "已下架";
            case 4 -> "已归档";
            default -> "未知";
        };
    }

    public static Integer getItemStatus(String status) {
        if (status == null) return null;
        return switch (status) {
            case "可互换" -> 1;
            case "互换中" -> 2;
            case "已下架" -> 3;
            case "已归档" -> 4;
            default -> null;
        };
    }

    public static String getPickupStatus(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待领取";
            case 1 -> "已领取";
            case 2 -> "已取消";
            case 3 -> "已过期";
            default -> "未知";
        };
    }

    public static Integer getPickupStatus(String status) {
        if (status == null) return null;
        return switch (status) {
            case "待领取" -> 0;
            case "已领取" -> 1;
            case "已取消" -> 2;
            case "已过期" -> 3;
            default -> null;
        };
    }

    public static String getExchangeStatus(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "审核通过";
            case 2 -> "已拒绝";
            case 3 -> "已完成";
            case 4 -> "已取消";
            default -> "未知";
        };
    }

    public static Integer getExchangeStatus(String status) {
        if (status == null) return null;
        return switch (status) {
            case "待审核" -> 0;
            case "审核通过" -> 1;
            case "已拒绝" -> 2;
            case "已完成" -> 3;
            case "已取消" -> 4;
            default -> null;
        };
    }

    public static String getPickupPointStatus(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 1 -> "启用";
            case 0 -> "停用";
            default -> "未知";
        };
    }

    public static Integer getPickupPointStatus(String status) {
        if (status == null) return null;
        return switch (status) {
            case "启用" -> 1;
            case "停用" -> 0;
            default -> null;
        };
    }

    public static String getUserStatus(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 1 -> "正常";
            case 0 -> "禁用";
            default -> "未知";
        };
    }

    public static String getCondition(Integer conditionLevel) {
        if (conditionLevel == null) return "未知";
        return switch (conditionLevel) {
            case 1 -> "全新";
            case 2 -> "九成新";
            case 3 -> "八成新";
            case 4 -> "七成新";
            case 5 -> "六成新";
            default -> "未知";
        };
    }

    public static Integer getCondition(String condition) {
        if (condition == null) return null;
        return switch (condition) {
            case "全新" -> 1;
            case "九成新" -> 2;
            case "八成新" -> 3;
            case "七成新" -> 4;
            case "六成新" -> 5;
            default -> null;
        };
    }

    public static String getArchiveType(String type) {
        if ("AUTO".equals(type)) return "自动";
        if ("MANUAL".equals(type)) return "手动";
        return type;
    }

    public static String getRoleName(Integer role) {
        if (role == null) return "普通用户";
        return role == 1 ? "管理员" : "普通用户";
    }
}
