package com.community.idle.common;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DataCompareUtil {

    private static final Set<String> IGNORE_FIELDS = new HashSet<>(Arrays.asList(
            "serialVersionUID", "createTime", "updateTime", "deleted", "password"
    ));

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return JSONUtil.toJsonStr(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }

    public static String compareAndGetSummary(Object before, Object after) {
        if (before == null && after == null) {
            return "";
        }
        if (before == null) {
            return "新增数据";
        }
        if (after == null) {
            return "删除数据";
        }
        if (before.getClass() != after.getClass()) {
            return "数据类型不一致";
        }

        List<String> changes = new ArrayList<>();
        Field[] fields = before.getClass().getDeclaredFields();

        for (Field field : fields) {
            String fieldName = field.getName();
            if (IGNORE_FIELDS.contains(fieldName)) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object beforeValue = field.get(before);
                Object afterValue = field.get(after);

                if (!Objects.equals(beforeValue, afterValue)) {
                    String change = formatChange(fieldName, beforeValue, afterValue);
                    if (StrUtil.isNotBlank(change)) {
                        changes.add(change);
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }

        if (changes.isEmpty()) {
            return "无数据变更";
        }

        String summary = String.join("; ", changes);
        if (summary.length() > 500) {
            summary = summary.substring(0, 497) + "...";
        }
        return summary;
    }

    private static String formatChange(String fieldName, Object beforeValue, Object afterValue) {
        String beforeStr = formatValue(beforeValue);
        String afterStr = formatValue(afterValue);

        String displayName = getFieldDisplayName(fieldName);
        if (StrUtil.isBlank(beforeStr) && StrUtil.isBlank(afterStr)) {
            return "";
        }
        if (StrUtil.isBlank(beforeStr)) {
            return displayName + " 设置为 " + afterStr;
        }
        if (StrUtil.isBlank(afterStr)) {
            return displayName + " 清空";
        }
        return displayName + ": " + beforeStr + " → " + afterStr;
    }

    private static String formatValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DATE_TIME_FORMATTER);
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).stripTrailingZeros().toPlainString();
        }
        if (value instanceof String) {
            String str = (String) value;
            if (str.length() > 50) {
                return str.substring(0, 47) + "...";
            }
            return str;
        }
        return value.toString();
    }

    private static String getFieldDisplayName(String fieldName) {
        Map<String, String> displayNames = new HashMap<>();
        displayNames.put("status", "状态");
        displayNames.put("username", "用户名");
        displayNames.put("nickname", "昵称");
        displayNames.put("phone", "手机号");
        displayNames.put("email", "邮箱");
        displayNames.put("creditScore", "信用分");
        displayNames.put("creditLevel", "信用等级");
        displayNames.put("itemName", "物品名称");
        displayNames.put("itemDesc", "物品描述");
        displayNames.put("category", "分类");
        displayNames.put("quantity", "数量");
        displayNames.put("originalValue", "原价");
        displayNames.put("conditionLevel", "成色");
        displayNames.put("pointName", "自提点名称");
        displayNames.put("address", "地址");
        displayNames.put("contactPerson", "联系人");
        displayNames.put("contactPhone", "联系电话");
        displayNames.put("businessHours", "营业时间");
        displayNames.put("maxCapacity", "最大容量");
        displayNames.put("roleName", "角色名称");
        displayNames.put("roleCode", "角色编码");
        displayNames.put("permissionName", "权限名称");
        displayNames.put("permissionCode", "权限编码");
        displayNames.put("changeValue", "变更分值");
        displayNames.put("changeReason", "变更原因");
        displayNames.put("rejectReason", "拒绝原因");
        displayNames.put("auditRemark", "审核备注");

        return displayNames.getOrDefault(fieldName, fieldName);
    }

    public static boolean isSignificantChange(BigDecimal beforeValue, BigDecimal afterValue, BigDecimal threshold) {
        if (beforeValue == null || afterValue == null || threshold == null) {
            return false;
        }
        BigDecimal change = afterValue.subtract(beforeValue).abs();
        return change.compareTo(threshold) >= 0;
    }
}
