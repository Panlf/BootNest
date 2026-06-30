package com.boot.intercept.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

/**
 * IP地址解析工具类
 *
 * <p>按优先级依次尝试从以下请求头获取客户端真实IP：</p>
 * <ol>
 *   <li>X-Forwarded-For（CDN/负载均衡常用）</li>
 *   <li>Proxy-Client-IP（Apache代理）</li>
 *   <li>WL-Proxy-Client-IP（WebLogic代理）</li>
 *   <li>remoteAddr（直连IP，本地调试时为127.0.0.1会尝试获取本机局域网IP）</li>
 * </ol>
 *
 * <p>对于多级代理场景，X-Forwarded-For可能包含多个IP，取第一个（客户端真实IP）。</p>
 */
@Slf4j
public final class IpUtil {

    private static final String UNKNOWN = "unknown";
    private static final String IP_HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String IP_HEADER_PROXY_CLIENT = "Proxy-Client-IP";
    private static final String IP_HEADER_WL_PROXY_CLIENT = "WL-Proxy-Client-IP";
    private static final String LOCALHOST = "127.0.0.1";

    private IpUtil() {
    }

    /**
     * 从请求中解析客户端真实IP地址
     *
     * @param request HTTP请求
     * @return 客户端IP地址，解析失败时返回空字符串
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader(IP_HEADER_X_FORWARDED_FOR);
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(IP_HEADER_PROXY_CLIENT);
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(IP_HEADER_WL_PROXY_CLIENT);
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (LOCALHOST.equals(ip)) {
                try {
                    ip = java.net.InetAddress.getLocalHost().getHostAddress();
                } catch (java.net.UnknownHostException e) {
                    log.error("获取本机IP地址失败", e);
                    ip = LOCALHOST;
                }
            }
        }
        // 多级代理时取第一个IP（客户端真实IP）
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return ip;
    }
}
