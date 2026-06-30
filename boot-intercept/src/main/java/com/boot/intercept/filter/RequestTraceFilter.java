package com.boot.intercept.filter;

import com.boot.intercept.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * 请求追踪过滤器（Servlet层 - 最外层）
 *
 * <p>职责：
 * 1. 为每个请求注入唯一标识 X-Request-Id，用于全链路日志追踪
 * 2. 解析客户端真实IP并写入 request attribute，供下游组件使用
 * 3. 记录请求进入和完成的日志，包含耗时统计</p>
 *
 * <p>执行顺序：Filter → Interceptor → Aspect → Controller</p>
 */
@Slf4j
@Component
public class RequestTraceFilter implements Filter {

    private static final String HEADER_REQUEST_ID = "X-Request-Id";
    private static final String HEADER_REAL_IP = "X-Real-IP";

    /**
     * 核心过滤逻辑：生成/透传请求ID，记录IP和耗时日志
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 优先从请求头获取已有的requestId（支持网关透传），否则生成新的
        String requestId = request.getHeader(HEADER_REQUEST_ID);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString().replace("-", "");
        }
        request.setAttribute(HEADER_REQUEST_ID, requestId);
        response.setHeader(HEADER_REQUEST_ID, requestId);

        // 解析真实IP并存入attribute，供Interceptor和Aspect使用
        String ip = IpUtil.getIpAddr(request);
        request.setAttribute(HEADER_REAL_IP, ip);

        log.info("[Filter] 请求进入 | requestId={} | ip={} | {} {}",
                requestId, ip, request.getMethod(), request.getRequestURI());

        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            long cost = System.currentTimeMillis() - startTime;
            log.info("[Filter] 请求完成 | requestId={} | status={} | cost={}ms",
                    requestId, response.getStatus(), cost);
        }
    }
}
