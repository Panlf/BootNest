package com.boot.intercept.constants;

public final class CacheKeyConstants {

    private CacheKeyConstants() {
    }

    public static final String RATE_LIMIT_PREFIX = "rate_limit:";

    public static final String BLACKLIST_PREFIX = "blacklist:";

    public static String buildRateLimitKey(String classMethod, String ip) {
        return RATE_LIMIT_PREFIX + classMethod + ":" + ip;
    }

    public static String buildBlacklistKey(String ip) {
        return BLACKLIST_PREFIX + ip;
    }
}
