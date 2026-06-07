package com.community.idle.common.enums;

public enum OperationType {

    EXCHANGE_APPROVE("EXCHANGE_APPROVE", "审核通过", false),
    EXCHANGE_REJECT("EXCHANGE_REJECT", "审核拒绝", false),
    ITEM_OFFLINE("ITEM_OFFLINE", "物品下架", false),
    ITEM_DELETE("ITEM_DELETE", "物品删除", false),
    CREDIT_ADJUST("CREDIT_ADJUST", "信用分调整", true),
    USER_DISABLE("USER_DISABLE", "用户禁用", true),
    USER_ENABLE("USER_ENABLE", "用户启用", true),
    PICKUP_POINT_ADD("PICKUP_POINT_ADD", "新增自提点", false),
    PICKUP_POINT_EDIT("PICKUP_POINT_EDIT", "编辑自提点", false),
    PICKUP_POINT_DELETE("PICKUP_POINT_DELETE", "删除自提点", true),
    ROLE_ASSIGN("ROLE_ASSIGN", "角色分配", true),
    ROLE_ADD("ROLE_ADD", "新增角色", true),
    ROLE_EDIT("ROLE_EDIT", "编辑角色", true),
    ROLE_DELETE("ROLE_DELETE", "删除角色", true),
    PERMISSION_CONFIG("PERMISSION_CONFIG", "权限配置", true),
    OTHER("OTHER", "其他操作", false);

    private final String code;
    private final String name;
    private final boolean sensitive;

    OperationType(String code, String name, boolean sensitive) {
        this.code = code;
        this.name = name;
        this.sensitive = sensitive;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public static OperationType getByCode(String code) {
        for (OperationType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return OTHER;
    }
}
