package com.boot.intercept.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 请求耗时统计拦截器（Spring MVC层）
 *
 * <p>职责：在Controller处理前后记录时间差，将耗时写入响应头 X-Response-Time，
 * 方便前端或网关监控接口性能。</p>
 *
 * <p>生命周期：</p>
 * <ul>
 *   <li>preHandle - Controller方法执行前，记录起始时间</li>
 *   <li>postHandle - Controller方法执行后、视图渲染前，计算耗时并写入响应头</li>
 *   <li>afterCompletion - 请求完全结束后，如有异常则记录错误日志</li>
 * </ul>
 *
 * <p>执行顺序：Filter → <b>TimingInterceptor(order=1)</b> → TokenInterceptor → Aspect → Controller</p>
 */
@Slf4j
@Component
public class RequestTimingInterceptor implements HandlerInterceptor {

    private static final String ATTR_START_TIME = "requestStartTime";
    private static final String HEADER_RESPONSE_TIME = "X-Response-Time";

    /**
     * 在请求进入Controller之前，记录当前时间戳到request attribute
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        request.setAttribute(ATTR_START_TIME, System.currentTimeMillis());
        log.debug("[Interceptor-Timing] preHandle | {} {}", request.getMethod(), request.getRequestURI());
        return true;
    }

    /**
     * Controller方法执行后，计算耗时并写入响应头，方便调用方监控
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
        Long startTime = (Long) request.getAttribute(ATTR_START_TIME);
        if (startTime != null) {
            long cost = System.currentTimeMillis() - startTime;
            response.setHeader(HEADER_RESPONSE_TIME, cost + "ms");
            log.debug("[Interceptor-Timing] postHandle | cost={}ms | {} {}",
                    cost, request.getMethod(), request.getRequestURI());
        }
    }

    /**
     * 请求完全结束后回调，如有异常则记录错误日志用于排查
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(ATTR_START_TIME);
        if (startTime != null) {
            long cost = System.currentTimeMillis() - startTime;
            if (ex != null) {
                log.error("[Interceptor-Timing] afterCompletion | 异常 cost={}ms | {} {} | {}",
                        cost, request.getMethod(), request.getRequestURI(), ex.getMessage());
            } else {
                log.debug("[Interceptor-Timing] afterCompletion | cost={}ms | status={}",
                        cost, response.getStatus());
            }
        }
    }
}
