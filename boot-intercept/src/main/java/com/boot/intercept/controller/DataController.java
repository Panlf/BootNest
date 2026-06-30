package com.boot.intercept.controller;

import com.boot.intercept.annotation.RateLimit;
import com.boot.intercept.common.bean.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 数据接口控制器
 *
 * <p>提供以下接口用于演示Filter → Interceptor → Aspect三层协作：</p>
 * <ul>
 *   <li>/api/public/greet - 公开接口，无需Token，演示Interceptor放行逻辑</li>
 *   <li>/api/health - 健康检查，无需Token</li>
 *   <li>/api/data/summary - 需要Token认证，演示拦截器鉴权</li>
 *   <li>/api/data/save - 需要Token + 限流(5次/秒)，演示Aspect限流</li>
 *   <li>/api/data/list - 需要Token + 限流(3次/秒)，演示限流触发429</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class DataController {

    /**
     * 公开问候接口 - 演示无需Token的路径放行
     */
    @GetMapping("/public/greet")
    public R<String> greet(@RequestParam(defaultValue = "World") String name) {
        return R.ok("Hello, " + name);
    }

    /**
     * 健康检查接口 - 供运维探活使用，无需认证
     */
    @GetMapping("/health")
    public R<String> health() {
        return R.ok("UP");
    }

    /**
     * 服务摘要接口 - 需要Token认证
     */
    @GetMapping("/data/summary")
    public R<Map<String, Object>> getSummary() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("service", "boot-intercept");
        data.put("status", "running");
        data.put("description", "Filter + Interceptor + Aspect 三层演示");
        return R.ok(data);
    }

    /**
     * 保存数据接口 - 需要Token认证 + 限流(每秒最多5次)
     */
    @PostMapping("/data/save")
    @RateLimit(maxRequests = 5, timeWindow = 1)
    public R<String> saveData(@RequestBody Map<String, String> payload) {
        log.info("保存数据 | payload={}", payload);
        return R.ok("数据保存成功");
    }

    /**
     * 数据列表接口 - 需要Token认证 + 限流(每秒最多3次)
     */
    @GetMapping("/data/list")
    @RateLimit(maxRequests = 3, timeWindow = 1)
    public R<Map<String, Object>> getDataList(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("page", page);
        result.put("size", size);
        result.put("total", 0);
        result.put("items", java.util.Collections.emptyList());
        return R.ok(result);
    }
}
