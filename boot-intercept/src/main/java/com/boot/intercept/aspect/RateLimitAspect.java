package com.boot.intercept.aspect;

import com.boot.intercept.annotation.RateLimit;
import com.boot.intercept.common.exception.BusinessException;
import com.boot.intercept.enums.ResultCodeEnum;
import com.boot.intercept.model.RateLimitInfo;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 接口限流切面（方法级 - 基于注解触发）
 *
 * <p>职责：通过 {@link RateLimit} 注解为指定接口设置访问频率限制，
 * 基于Caffeine本地缓存实现滑动窗口计数，超出阈值直接抛出429异常。</p>
 *
 * <p>使用方式：在Controller方法上添加注解即可生效</p>
 * <pre>
 * {@literal @}RateLimit(maxRequests = 5, timeWindow = 1)
 * public R&lt;String&gt; saveData(...) { ... }
 * </pre>
 *
 * <p>限流维度：方法签名 + 客户端IP（同一IP对不同方法独立计数）</p>
 *
 * <p>工作流程：</p>
 * <ol>
 *   <li>从Caffeine缓存中获取当前窗口的计数信息</li>
 *   <li>如果窗口未过期，累加计数并判断是否超限</li>
 *   <li>如果窗口已过期或不存在，重置计数重新开始</li>
 *   <li>超出阈值时抛出 BusinessException，由 GlobalExceptionHandler 返回429</li>
 * </ol>
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    @Resource(name = "rateLimitCache")
    private Cache<String, RateLimitInfo> rateLimitCache;

    /**
     * 环绕通知：拦截所有标注了 @RateLimit 的方法，执行限流校验
     *
     * @param joinPoint  连接点，用于获取方法信息和执行原方法
     * @param rateLimit  注解实例，包含maxRequests和timeWindow配置
     * @return 原方法的返回值（未触发限流时）
     * @throws BusinessException 触发限流时抛出
     */
    @Around("@annotation(rateLimit)")
    public Object doRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 构建限流key：方法名:IP
        String ip = request.getRemoteAddr();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String classMethod = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        String key = classMethod + ":" + ip;

        RateLimitInfo info = rateLimitCache.getIfPresent(key);
        long now = System.currentTimeMillis();
        long windowMillis = rateLimit.timeUnit().toMillis(rateLimit.timeWindow());

        if (info != null && (now - info.getWindowStart()) < windowMillis) {
            // 窗口未过期，累加计数
            info.increment();
            if (info.getCount() > rateLimit.maxRequests()) {
                log.warn("[Aspect-RateLimit] 触发限流 | key={} | count={}/{}",
                        key, info.getCount(), rateLimit.maxRequests());
                throw new BusinessException(ResultCodeEnum.RATE_LIMIT);
            }
        } else {
            // 窗口已过期或首次访问，重置计数
            info = new RateLimitInfo(key, now);
        }
        rateLimitCache.put(key, info);

        log.debug("[Aspect-RateLimit] 通过 | key={} | count={}/{}",
                key, info.getCount(), rateLimit.maxRequests());
        return joinPoint.proceed();
    }
}
