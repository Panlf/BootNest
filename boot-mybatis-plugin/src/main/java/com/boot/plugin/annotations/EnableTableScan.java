package com.boot.plugin.annotations;

import java.lang.annotation.*;

/**
 * 是否启用全量扫描的拦截 true则需要拦截
 * @author panlf
 * @date 2022/7/13
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface EnableTableScan {
    boolean enable() default false;
}
