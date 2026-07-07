package com.boot.scheduled.configuration;

import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.executor.ScheduledDtpExecutor;
import org.dromara.dynamictp.core.spring.EnableDynamicTp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 动态线程池配置
 * <p>
 * 使用 dynamictp 实现线程池的动态调参和监控能力。
 * cron 调度线程池和手动触发线程池均由 dynamictp 管理。
 * </p>
 */
@Configuration
@EnableDynamicTp
public class DynamicTpConfig {

    /**
     * cron 调度动态线程池
     * <p>
     * 使用 ScheduledDtpExecutor，支持动态调参和监控。
     * </p>
     */
    @Bean("scheduleDtpExecutor")
    public ScheduledDtpExecutor scheduleDtpExecutor() {
        AtomicInteger counter = new AtomicInteger(0);
        ThreadFactory factory = r -> {
            Thread t = new Thread(r, "schedule-task-" + counter.incrementAndGet());
            t.setDaemon(true);
            return t;
        };

        ScheduledDtpExecutor executor = new ScheduledDtpExecutor(
                20, 20, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(Integer.MAX_VALUE),
                factory,
                new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy()
        );
        executor.setThreadPoolName("scheduleDtpExecutor");
        executor.setRejectHandlerType("CallerRunsPolicy");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        return executor;
    }

    /**
     * 调度线程池 - 重写 createExecutor 以使用 dynamictp 的 ScheduledDtpExecutor
     */
    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler(ScheduledDtpExecutor scheduleDtpExecutor) {
        return new ThreadPoolTaskScheduler() {
            @Override
            protected ScheduledExecutorService createExecutor(int poolSize, ThreadFactory threadFactory,
                                                               RejectedExecutionHandler rejectedHandler) {
                return scheduleDtpExecutor.getOriginal();
            }
        };
    }

    /**
     * 手动触发线程池，通过 dynamictp 管理，支持动态调参和监控
     */
    @Bean("asyncExecutor")
    public DtpExecutor asyncExecutor() {
        DtpExecutor executor = new DtpExecutor(
                10, 50, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200),
                new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy()
        );
        executor.setThreadPoolName("asyncExecutor");
        executor.setRejectHandlerType("CallerRunsPolicy");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        return executor;
    }
}
