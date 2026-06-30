package com.boot.intercept.config;

import com.boot.intercept.model.RateLimitInfo;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine本地缓存配置
 *
 * <p>为 {@link com.boot.intercept.aspect.RateLimitAspect} 提供限流计数的本地缓存存储。</p>
 * <p>策略：写入后1分钟自动过期，最大容量10000条，足以应对单机限流场景。</p>
 */
@Configuration
public class CacheConfig {

    /**
     * 限流计数缓存：key=方法名:IP，value=当前窗口的访问计数信息
     */
    @Bean("rateLimitCache")
    public Cache<String, RateLimitInfo> rateLimitCache() {
        return Caffeine.<String, RateLimitInfo>newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .initialCapacity(128)
                .maximumSize(10000)
                .build();
    }
}
