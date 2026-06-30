package com.boot.intercept.aspect;

import com.boot.intercept.common.bean.RequestInfo;
import com.boot.intercept.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 请求日志切面（方法级 - 最内层）
 *
 * <p>职责：环绕Controller所有方法，自动记录每次请求的入参、返回值、耗时和异常信息，
 * 实现业务代码零侵入的统一日志采集。</p>
 *
 * <p>切入点：com.boot.intercept.controller 包下所有方法</p>
 *
 * <p>日志格式示例：</p>
 * <pre>
 * [Aspect-Log] >>> 请求入参 | {requestId=xxx, ip=127.0.0.1, url=/api/data/list, ...}
 * [Aspect-Log] <<< 请求返回 | {requestId=xxx, timeCost=15ms, ...}
 * [Aspect-Log] !!! 请求异常 | {requestId=xxx, error=xxx, ...}
 * </pre>
 */
@Slf4j
@Aspect
@Component
public class RequestLogAspect {

    /** 切入controller包下所有public方法 */
    @Pointcut("execution(* com.boot.intercept.controller..*(..))")
    public void controllerPointcut() {
    }

    /**
     * 环绕通知：在Controller方法执行前后分别记录日志
     * - 执行前：记录请求ID、IP、URL、HTTP方法、方法签名、入参
     * - 执行后：记录返回值和耗时
     * - 异常时：记录异常信息并重新抛出
     */
    @Around("controllerPointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 构建请求信息对象，用于结构化日志输出
        RequestInfo info = new RequestInfo();
        info.setRequestId((String) request.getAttribute("X-Request-Id"));
        info.setIp(IpUtil.getIpAddr(request));
        info.setUrl(request.getRequestURL().toString());
        info.setHttpMethod(request.getMethod());
        info.setClassMethod(formatClassMethod(joinPoint));
        info.setRequestParams(extractParams(joinPoint));

        log.info("[Aspect-Log] >>> 请求入参 | {}", info);

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            info.setResult(result);
            info.setTimeCost(System.currentTimeMillis() - start);
            log.info("[Aspect-Log] <<< 请求返回 | {}", info);
            return result;
        } catch (Throwable ex) {
            info.setIsError(true);
            info.setErrorMessage(ex.getMessage());
            info.setTimeCost(System.currentTimeMillis() - start);
            log.error("[Aspect-Log] !!! 请求异常 | {}", info);
            throw ex;
        }
    }

    /**
     * 格式化类名.方法名，如 DataController.getDataList
     */
    private String formatClassMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringType().getSimpleName() + "." + signature.getName();
    }

    /**
     * 提取方法参数，以参数名-参数值的Map形式返回
     * 对文件类型参数特殊处理，只记录文件名而非文件内容
     */
    private Map<String, Object> extractParams(ProceedingJoinPoint joinPoint) {
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        Object[] paramValues = joinPoint.getArgs();
        Map<String, Object> params = new LinkedHashMap<>();
        for (int i = 0; i < paramNames.length; i++) {
            Object value = paramValues[i];
            if (value instanceof MultipartFile) {
                value = ((MultipartFile) value).getOriginalFilename();
            }
            params.put(paramNames[i], value);
        }
        return params;
    }
}
