package com.boot.intercept.model;

import lombok.Data;

/**
 * 限流计数信息
 *
 * <p>存储在Caffeine缓存中，记录某个接口+IP在当前时间窗口内的访问计数。</p>
 */
@Data
public class RateLimitInfo {

    /** 限流key（格式：方法名:IP） */
    private String key;

    /** 当前窗口内的请求计数 */
    private int count;

    /** 当前窗口的起始时间戳（毫秒） */
    private long windowStart;

    public RateLimitInfo(String key, long windowStart) {
        this.key = key;
        this.count = 1;
        this.windowStart = windowStart;
    }

    /** 计数器自增并返回新值 */
    public int increment() {
        return ++this.count;
    }
}
