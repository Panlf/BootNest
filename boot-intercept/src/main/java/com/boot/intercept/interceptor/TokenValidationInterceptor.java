package com.boot.intercept.interceptor;

import com.boot.intercept.common.bean.R;
import com.boot.intercept.enums.ResultCodeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Token身份认证拦截器（Spring MVC层）
 *
 * <p>职责：校验请求头中的Authorization Token，未携带或无效则直接返回401，
 * 通过后将用户ID写入request attribute供Controller获取。</p>
 *
 * <p>拦截规则（在WebMvcConfig中配置）：</p>
 * <ul>
 *   <li>拦截路径：/api/**</li>
 *   <li>排除路径：/api/public/**、/api/health（无需登录的公开接口）</li>
 * </ul>
 *
 * <p>执行顺序：Filter → TimingInterceptor(order=1) → <b>TokenInterceptor(order=2)</b> → Aspect → Controller</p>
 */
@Slf4j
@Component
public class TokenValidationInterceptor implements HandlerInterceptor {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String ATTR_USER_ID = "currentUserId";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 请求到达Controller前校验Token：
     * 1. 公开路径直接放行
     * 2. 无Token返回401
     * 3. Token无效返回401
     * 4. 验证通过则将userId存入attribute
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        String uri = request.getRequestURI();
        if (isPublicPath(uri)) {
            return true;
        }

        String token = request.getHeader(HEADER_AUTHORIZATION);
        if (token == null || token.isEmpty()) {
            log.warn("[Interceptor-Token] 未携带Token | ip={} | uri={}",
                    request.getRemoteAddr(), uri);
            writeUnauthorized(response, "请先登录");
            return false;
        }

        String userId = validateToken(token);
        if (userId == null) {
            log.warn("[Interceptor-Token] Token无效或已过期 | ip={} | uri={}",
                    request.getRemoteAddr(), uri);
            writeUnauthorized(response, "Token无效或已过期");
            return false;
        }

        // 将解析出的用户ID存入request，Controller可通过 request.getAttribute("currentUserId") 获取
        request.setAttribute(ATTR_USER_ID, userId);
        log.debug("[Interceptor-Token] Token验证通过 | userId={} | uri={}", userId, uri);
        return true;
    }

    /**
     * 判断是否为公开路径，无需Token校验
     */
    private boolean isPublicPath(String uri) {
        return uri.startsWith("/api/public") || uri.equals("/api/health");
    }

    /**
     * 解析Token并返回用户ID，校验失败返回null
     * 实际项目中应替换为JWT解析或Redis查询
     */
    private String validateToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // 演示用：test-token-{userId} 格式视为合法Token
        if (token.startsWith("test-token-")) {
            return token.substring("test-token-".length());
        }
        return null;
    }

    /**
     * 向客户端写入401未授权响应（JSON格式）
     */
    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        R<?> result = R.fail(ResultCodeEnum.UNAUTHORIZED.getCode(), message);
        response.getWriter().write(MAPPER.writeValueAsString(result));
    }
}
