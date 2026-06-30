package com.boot.intercept.config;

import com.boot.intercept.common.bean.R;
import com.boot.intercept.common.exception.BusinessException;
import com.boot.intercept.enums.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * <p>统一捕获Controller层抛出的异常，返回标准格式的错误响应。</p>
 * <ul>
 *   <li>BusinessException - 业务异常，返回对应错误码（如429限流、403禁止）</li>
 *   <li>Exception - 未知异常，返回500内部错误，记录完整堆栈</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常（限流、权限不足等已知错误）
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<?>> handleBusinessException(BusinessException e) {
        log.warn("[全局异常] 业务异常 | code={} | message={}", e.getCode(), e.getMessage());
        return ResponseEntity.status(e.getCode()).body(R.fail(e.getCode(), e.getMessage()));
    }

    /**
     * 处理未预期的系统异常，记录完整堆栈便于排查
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<?>> handleException(Exception e) {
        log.error("[全局异常] 系统异常", e);
        return ResponseEntity.status(500).body(R.fail(ResultCodeEnum.INTERNAL_ERROR));
    }
}
