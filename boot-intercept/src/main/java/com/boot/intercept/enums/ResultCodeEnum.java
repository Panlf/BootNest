package com.boot.intercept.enums;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {

    SUCCESS(true, 200, "操作成功"),
    BAD_REQUEST(false, 400, "请求参数错误"),
    UNAUTHORIZED(false, 401, "未授权，请先登录"),
    FORBIDDEN(false, 403, "禁止访问"),
    NOT_FOUND(false, 404, "资源不存在"),
    RATE_LIMIT(false, 429, "请求过于频繁，请稍后再试"),
    INTERNAL_ERROR(false, 500, "服务器内部错误");

    private final Boolean success;
    private final Integer code;
    private final String message;

    ResultCodeEnum(Boolean success, Integer code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }
}
