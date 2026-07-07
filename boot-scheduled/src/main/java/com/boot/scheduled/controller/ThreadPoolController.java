package com.boot.scheduled.controller;

import com.boot.scheduled.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.executor.ScheduledDtpExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "动态线程池管理")
@RestController
@RequestMapping("/api/thread-pool")
public class ThreadPoolController {

    private final ScheduledDtpExecutor scheduleDtpExecutor;
    private final DtpExecutor asyncExecutor;

    public ThreadPoolController(@Qualifier("scheduleDtpExecutor") ScheduledDtpExecutor scheduleDtpExecutor,
                                @Qualifier("asyncExecutor") DtpExecutor asyncExecutor) {
        this.scheduleDtpExecutor = scheduleDtpExecutor;
        this.asyncExecutor = asyncExecutor;
    }

    @Operation(summary = "查询所有线程池状态")
    @GetMapping("/status")
    public Result<Map<String, Object>> getAllPoolStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("scheduleDtpExecutor", buildPoolInfo(scheduleDtpExecutor.getOriginal()));
        result.put("asyncExecutor", buildPoolInfo(asyncExecutor));
        return Result.success(result);
    }

    @Operation(summary = "修改调度线程池参数")
    @PutMapping("/schedule")
    public Result<Void> updateSchedulePool(@RequestParam Integer corePoolSize,
                                           @RequestParam Integer maximumPoolSize) {
        scheduleDtpExecutor.getOriginal().setCorePoolSize(corePoolSize);
        scheduleDtpExecutor.getOriginal().setMaximumPoolSize(maximumPoolSize);
        return Result.success();
    }

    @Operation(summary = "修改手动触发线程池参数")
    @PutMapping("/async")
    public Result<Void> updateAsyncPool(@RequestParam Integer corePoolSize,
                                        @RequestParam Integer maximumPoolSize) {
        asyncExecutor.setCorePoolSize(corePoolSize);
        asyncExecutor.setMaximumPoolSize(maximumPoolSize);
        return Result.success();
    }

    private Map<String, Object> buildPoolInfo(java.util.concurrent.ThreadPoolExecutor executor) {
        Map<String, Object> info = new HashMap<>();
        info.put("corePoolSize", executor.getCorePoolSize());
        info.put("maximumPoolSize", executor.getMaximumPoolSize());
        info.put("poolSize", executor.getPoolSize());
        info.put("activeCount", executor.getActiveCount());
        info.put("queueSize", executor.getQueue().size());
        info.put("completedTaskCount", executor.getCompletedTaskCount());
        info.put("taskCount", executor.getTaskCount());
        return info;
    }
}
