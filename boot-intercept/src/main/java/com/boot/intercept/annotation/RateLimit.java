package com.boot.intercept.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流注解
 *
 * <p>标注在Controller方法上，由 {@link com.boot.intercept.aspect.RateLimitAspect} 切面拦截并执行限流校验。</p>
 *
 * <p>示例：限制某接口每秒最多5次访问</p>
 * <pre>
 * {@literal @}RateLimit(maxRequests = 5, timeWindow = 1)
 * public R&lt;String&gt; saveData(...) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** 时间窗口内允许的最大请求数 */
    int maxRequests() default 10;

    /** 时间窗口大小 */
    int timeWindow() default 1;

    /** 时间窗口单位，默认秒 */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
