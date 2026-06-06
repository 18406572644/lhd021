package com.community.idle.common;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "系统错误"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),

    USER_NOT_EXIST(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_DISABLED(1003, "用户已被禁用"),
    USER_EXIST(1004, "用户已存在"),

    ITEM_NOT_EXIST(2001, "物品不存在"),
    ITEM_STATUS_ERROR(2002, "物品状态错误"),
    ITEM_OFFLINE(2003, "物品已下架"),

    EXCHANGE_NOT_EXIST(3001, "互换申请不存在"),
    EXCHANGE_STATUS_ERROR(3002, "互换申请状态错误"),
    EXCHANGE_ALREADY_PROCESSED(3003, "互换申请已处理"),

    PICKUP_NOT_EXIST(4001, "自提点不存在"),
    PICKUP_IN_USE(4002, "自提点正在使用中"),

    CREDIT_INSUFFICIENT(5001, "用户信用不足"),

    ARCHIVE_ERROR(6001, "归档失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
