package com.boot.scheduled.dto;

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;

import com.boot.scheduled.job.BaseScheduledTask;
import lombok.Data;

/**
 * 定时任务运行时数据传输对象
 * <p>
 * 存储任务的cron表达式、任务实例、调度Future及运行状态。
 * 当任务实例为 {@link BaseScheduledTask} 时，自动提取运行状态信息。
 * </p>
 *
 * @author MiMoCode
 */
@Data
public class TaskListDto {

    /** cron调度表达式 */
    private String cron;

    /** 任务实例（Runnable对象） */
    private Object task;

    /** 调度Future句柄，用于控制任务的启停 */
    private ScheduledFuture<?> future;

    /** 任务名称（从BaseScheduledTask提取） */
    private String taskName;

    /** 是否正在运行（单次执行是否在进行中） */
    private boolean running;

    /** 是否已注册调度（true=调度中, false=已暂停） */
    private boolean scheduled = true;

    /** 最近一次执行开始时间 */
    private LocalDateTime lastStartTime;

    /** 最近一次执行结束时间 */
    private LocalDateTime lastEndTime;

    /** 累计执行次数 */
    private long totalExecutionCount;

    /** 累计失败次数 */
    private long failureCount;

    /** 最近一次执行的异常信息 */
    private String lastError;

    /**
     * 从任务实例中同步状态信息
     * <p>如果任务实例是 BaseScheduledTask，自动提取运行状态。</p>
     */
    public void syncStatusFromTask() {
        if (task instanceof BaseScheduledTask) {
            BaseScheduledTask scheduledTask = (BaseScheduledTask) task;
            this.taskName = scheduledTask.getTaskName();
            this.running = scheduledTask.isRunning();
            this.lastStartTime = scheduledTask.getLastStartTime();
            this.lastEndTime = scheduledTask.getLastEndTime();
            this.totalExecutionCount = scheduledTask.getTotalExecutionCount();
            this.failureCount = scheduledTask.getFailureCount();
            this.lastError = scheduledTask.getLastError();
        }
    }
}
