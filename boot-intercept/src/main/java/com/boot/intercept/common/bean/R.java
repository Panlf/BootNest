package com.boot.intercept.common.bean;

import com.boot.intercept.enums.ResultCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应对象
 *
 * <p>所有接口统一返回格式：{success, code, message, data}</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class R<T> {

    /** 请求是否成功 */
    private Boolean success;

    /** 业务状态码 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 返回数据 */
    private T data;

    /** 成功响应 - 携带数据 */
    public static <T> R<T> ok(T data) {
        return new R<>(true, ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMessage(), data);
    }

    /** 成功响应 - 自定义提示 + 数据 */
    public static <T> R<T> ok(String message, T data) {
        return new R<>(true, ResultCodeEnum.SUCCESS.getCode(), message, data);
    }

    /** 失败响应 - 使用枚举定义的错误码 */
    public static <T> R<T> fail(ResultCodeEnum resultCode) {
        return new R<>(false, resultCode.getCode(), resultCode.getMessage(), null);
    }

    /** 失败响应 - 枚举错误码 + 附加数据 */
    public static <T> R<T> fail(ResultCodeEnum resultCode, T data) {
        return new R<>(false, resultCode.getCode(), resultCode.getMessage(), data);
    }

    /** 失败响应 - 自定义错误码和消息 */
    public static <T> R<T> fail(Integer code, String message) {
        return new R<>(false, code, message, null);
    }
}
