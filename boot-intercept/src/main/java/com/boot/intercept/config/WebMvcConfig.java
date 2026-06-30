package com.boot.intercept.config;

import com.boot.intercept.interceptor.RequestTimingInterceptor;
import com.boot.intercept.interceptor.TokenValidationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * Web MVC配置 - 注册拦截器及其执行顺序
 *
 * <p>拦截器执行顺序由order决定，数值越小越先执行：</p>
 * <ol>
 *   <li>RequestTimingInterceptor (order=1) - 所有请求，记录耗时</li>
 *   <li>TokenValidationInterceptor (order=2) - 仅/api/**路径，校验Token</li>
 * </ol>
 *
 * <p>完整的请求处理链路：</p>
 * <pre>
 * Request → Filter → TimingInterceptor(1) → TokenInterceptor(2) → Aspect → Controller
 * </pre>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private RequestTimingInterceptor requestTimingInterceptor;

    @Resource
    private TokenValidationInterceptor tokenValidationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 耗时统计拦截器：拦截所有路径，最先执行
        registry.addInterceptor(requestTimingInterceptor)
                .addPathPatterns("/**")
                .order(1);

        // Token认证拦截器：仅拦截/api/**，排除公开接口
        registry.addInterceptor(tokenValidationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/public/**", "/api/health")
                .order(2);
    }
}
