package com.boot.scheduled.common;

import lombok.Getter;

/**
 * 业务异常
 *
 * @author MiMoCode
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
