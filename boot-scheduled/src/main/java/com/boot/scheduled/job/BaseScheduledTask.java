package com.boot.scheduled.job;

import com.boot.scheduled.bean.TaskLog;
import com.boot.scheduled.configuration.ApplicationContextProvider;
import com.boot.scheduled.service.TaskLogService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 定时任务抽象基类
 * <p>
 * 所有定时任务应继承此类并实现 {@link #execute()} 方法。
 * 基类自动管理任务的生命周期，包括：
 * <ul>
 *   <li>记录每次执行的开始时间和结束时间</li>
 *   <li>跟踪任务是否正在运行</li>
 *   <li>捕获并记录执行过程中的异常</li>
 *   <li>统计累计执行次数和失败次数（原子操作，线程安全）</li>
 *   <li>将每次执行记录持久化到 task_log 数据表</li>
 *   <li>支持并发控制：上一次未执行完毕时，可选择跳过本次执行</li>
 * </ul>
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * public class MyJob extends BaseScheduledTask {
 *     &#64;Override
 *     protected void execute() {
 *         // 业务逻辑
 *     }
 * }
 * </pre>
 *
 * @author MiMoCode
 */
@Slf4j
@Getter
public abstract class BaseScheduledTask implements Runnable {

    /** 任务名称，子类可通过构造函数设置 */
    private final String taskName;

    /** 关联的任务数据库ID，由MapContainer注册时设置 */
    @Setter
    private Integer taskId;

    /** 任务类全限定名，由MapContainer注册时设置 */
    @Setter
    private String taskClass;

    /** 运行时参数，由手动触发时传入 */
    @Setter
    private Map<String, String> params = new HashMap<>();

    /** 是否允许并发执行（true=允许，false=上一次未完成时跳过） */
    @Setter
    private boolean allowConcurrent = true;

    /** 本次执行是否强制执行（由手动触发时设置，run结束后重置为false） */
    @Setter
    private volatile boolean forceExecution = false;

    /** 最近一次执行的开始时间 */
    private volatile LocalDateTime lastStartTime;

    /** 最近一次执行的结束时间 */
    private volatile LocalDateTime lastEndTime;

    /** 任务当前是否正在执行 */
    private volatile boolean running;

    /** 最近一次执行的异常信息，正常执行则为null */
    private volatile String lastError;

    /** 累计执行总次数（原子操作，线程安全） */
    private final AtomicLong totalExecutionCount = new AtomicLong(0);

    /** 累计失败次数（原子操作，线程安全） */
    private final AtomicLong failureCount = new AtomicLong(0);

    protected BaseScheduledTask() {
        this.taskName = this.getClass().getSimpleName();
    }

    protected BaseScheduledTask(String taskName) {
        this.taskName = taskName;
    }

    /**
     * 子类实现此方法完成具体业务逻辑
     *
     * @throws Exception 业务异常
     */
    protected abstract void execute() throws Exception;

    /**
     * 任务执行入口，由线程池调度器调用
     * <p>
     * 自动记录开始/结束时间，捕获异常，更新运行状态，并将执行记录持久化到数据库。
     * 当 allowConcurrent=false 且任务正在执行时，本次执行将被跳过。
     * 不要在子类中重写此方法，应重写 {@link #execute()}。
     * </p>
     */
    @Override
    public final void run() {
        boolean forced = forceExecution;
        // 并发控制：如果不允许并发、非强制执行且上一次未完成，跳过本次
        if (!allowConcurrent && !forced && running) {
            log.warn("[{}] 任务正在执行中，本次触发被跳过 (allowConcurrent=false, forceExec=false)", taskName);
            return;
        }

        lastStartTime = LocalDateTime.now();
        running = true;
        lastError = null;
        totalExecutionCount.incrementAndGet();

        String threadName = Thread.currentThread().getName();
        log.info("[{}] 任务开始执行, 线程: {}, 强制执行: {}", taskName, threadName, forced);

        TaskLog taskLog = new TaskLog();
        taskLog.setTaskId(taskId);
        taskLog.setTaskName(taskName);
        taskLog.setTaskClass(taskClass);
        taskLog.setStartTime(lastStartTime);
        taskLog.setThreadName(threadName);
        taskLog.setCreateTime(LocalDateTime.now());

        try {
            execute();
            taskLog.setStatus(1);
            log.info("[{}] 任务执行成功, 耗时: {}ms",
                    taskName, java.time.Duration.between(lastStartTime, LocalDateTime.now()).toMillis());
        } catch (Exception e) {
            failureCount.incrementAndGet();
            lastError = e.getMessage();
            taskLog.setStatus(0);
            taskLog.setErrorMsg(e.getMessage());
            log.error("[{}] 任务执行失败, 错误: {}", taskName, e.getMessage(), e);
        } finally {
            lastEndTime = LocalDateTime.now();
            running = false;
            forceExecution = false;
            taskLog.setEndTime(lastEndTime);
            persistLog(taskLog);
        }
    }

    /**
     * 将执行日志持久化到数据库
     *
     * @param taskLog 日志记录
     */
    private void persistLog(TaskLog taskLog) {
        try {
            TaskLogService taskLogService = ApplicationContextProvider.getBean(TaskLogService.class);
            taskLogService.save(taskLog);
        } catch (Exception e) {
            log.error("[{}] 保存执行日志失败: {}", taskName, e.getMessage());
        }
    }

    /**
     * 获取指定参数值，不存在时返回默认值
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    protected String getParam(String name, String defaultValue) {
        return params.getOrDefault(name, defaultValue);
    }

    /**
     * 获取指定参数值，不存在时返回null
     *
     * @param name 参数名
     * @return 参数值或null
     */
    protected String getParam(String name) {
        return params.get(name);
    }

    /**
     * 获取指定参数并转为Integer，不存在或转换失败时返回默认值
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    protected Integer getParamAsInt(String name, Integer defaultValue) {
        String value = params.get(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取任务运行状态摘要
     *
     * @return 状态描述字符串
     */
    public String getStatusSummary() {
        long total = totalExecutionCount.get();
        long fail = failureCount.get();
        long success = total - fail;
        return String.format("%s{running=%s, allowConcurrent=%s, total=%d, success=%d, failure=%d, lastStart=%s, lastEnd=%s, lastError=%s}",
                taskName, running, allowConcurrent, total, success, fail,
                lastStartTime, lastEndTime, lastError);
    }

    /** 获取累计执行次数 */
    public long getTotalExecutionCount() {
        return totalExecutionCount.get();
    }

    /** 获取累计失败次数 */
    public long getFailureCount() {
        return failureCount.get();
    }
}
