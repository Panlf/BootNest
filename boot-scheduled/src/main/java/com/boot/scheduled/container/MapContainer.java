package com.boot.scheduled.container;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executor;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import com.boot.scheduled.bean.TaskList;
import com.boot.scheduled.common.BusinessException;
import com.boot.scheduled.dto.TaskListDto;
import com.boot.scheduled.job.BaseScheduledTask;

import lombok.extern.slf4j.Slf4j;

/**
 * 定时任务内存容器
 * <p>
 * 使用 ConcurrentHashMap 存储所有已注册的定时任务实例。
 * 暂停只取消调度（future.cancel），任务仍保留在容器中，状态可查询。
 * 删除才会从容器中真正移除。
 * </p>
 *
 * @author MiMoCode
 */
@Slf4j
@Component
public class MapContainer {

    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private final Executor asyncExecutor;
    private final Map<Integer, TaskListDto> taskRegistry = new ConcurrentHashMap<>();

    public MapContainer(ThreadPoolTaskScheduler threadPoolTaskScheduler,
                        @org.springframework.beans.factory.annotation.Qualifier("asyncExecutor") Executor asyncExecutor) {
        this.threadPoolTaskScheduler = threadPoolTaskScheduler;
        this.asyncExecutor = asyncExecutor;
    }

    /**
     * 根据任务ID获取任务实例
     *
     * @param id 任务主键ID
     * @return 任务DTO，不存在时返回null
     */
    public TaskListDto getById(Integer id) {
        return taskRegistry.get(id);
    }

    /**
     * 获取当前所有任务的注册表
     *
     * @return 任务注册表
     */
    public Map<Integer, TaskListDto> getMapContainer() {
        return taskRegistry;
    }

    /**
     * 将任务注册到容器并启动调度
     *
     * @param taskList 任务信息
     * @return 任务DTO
     */
    public TaskListDto putMap(TaskList taskList) {
        TaskListDto taskListDto = new TaskListDto();
        taskListDto.setCron(taskList.getCron());
        taskListDto.setScheduled(true);

        Object obj;
        try {
            obj = Class.forName(taskList.getClazz()).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("任务类实例化失败, id={}, class={}, error={}", taskList.getId(), taskList.getClazz(), e.getMessage());
            throw new BusinessException("任务类实例化失败: " + taskList.getClazz());
        }

        if (!(obj instanceof Runnable)) {
            throw new BusinessException("任务类未实现Runnable接口: " + taskList.getClazz());
        }

        if (obj instanceof BaseScheduledTask) {
            BaseScheduledTask scheduledTask = (BaseScheduledTask) obj;
            scheduledTask.setTaskId(taskList.getId());
            scheduledTask.setTaskClass(taskList.getClazz());
            scheduledTask.setAllowConcurrent(taskList.getAllowConcurrent() == null || taskList.getAllowConcurrent() == 1);
        }

        taskListDto.setTask(obj);
        ScheduledFuture<?> future = threadPoolTaskScheduler.schedule(
                (Runnable) obj, new CronTrigger(taskList.getCron()));
        taskListDto.setFuture(future);
        taskRegistry.put(taskList.getId(), taskListDto);
        log.info("定时任务已注册, id={}, class={}, cron={}", taskList.getId(), taskList.getClazz(), taskList.getCron());
        return taskListDto;
    }

    /**
     * 暂停任务调度（保留容器中的记录，可查询状态）
     *
     * @param id 任务主键ID
     * @return 任务DTO
     */
    public TaskListDto cancelMap(Integer id) {
        TaskListDto task = taskRegistry.get(id);
        if (task != null) {
            if (task.getFuture() != null) {
                task.getFuture().cancel(true);
            }
            task.setScheduled(false);
            log.info("定时任务已暂停, id={}", id);
        }
        return task;
    }

    /**
     * 删除任务（从容器中彻底移除）
     *
     * @param id 任务主键ID
     * @return 被移除的任务DTO
     */
    public TaskListDto deleteMap(Integer id) {
        TaskListDto task = taskRegistry.remove(id);
        if (task != null && task.getFuture() != null) {
            task.getFuture().cancel(true);
            log.info("定时任务已删除, id={}", id);
        }
        return task;
    }

    /**
     * 重新启动定时任务
     *
     * @param taskList 任务信息
     * @return 任务DTO
     */
    public TaskListDto restartMap(TaskList taskList) {
        return putMap(taskList);
    }

    /**
     * 手动触发执行一次任务（默认参数，不强制执行）
     *
     * @param id 任务主键ID
     * @return 任务DTO
     */
    public TaskListDto triggerTask(Integer id) {
        return triggerTask(id, new HashMap<>(), false);
    }

    /**
     * 手动触发执行一次任务（带参数，不强制执行）
     *
     * @param id     任务主键ID
     * @param params 运行时参数
     * @return 任务DTO
     */
    public TaskListDto triggerTask(Integer id, Map<String, String> params) {
        return triggerTask(id, params, false);
    }

    /**
     * 手动触发执行一次任务（完整版）
     * <p>
     * forceExec=true 时，忽略 allowConcurrent 设置，强制执行一次。
     * forceExec=false 时，如果任务正在执行且 allowConcurrent=false，本次触发被跳过。
     * </p>
     *
     * @param id        任务主键ID
     * @param params    运行时参数
     * @param forceExec 是否强制执行
     * @return 任务DTO
     */
    public TaskListDto triggerTask(Integer id, Map<String, String> params, boolean forceExec) {
        TaskListDto dto = taskRegistry.get(id);
        if (dto == null) {
            throw new BusinessException("任务未注册, id=" + id);
        }
        Object task = dto.getTask();
        if (!(task instanceof Runnable)) {
            throw new BusinessException("任务实例不是Runnable, id=" + id);
        }
        if (task instanceof BaseScheduledTask) {
            BaseScheduledTask scheduledTask = (BaseScheduledTask) task;
            scheduledTask.setParams(params != null ? params : new HashMap<>());
            scheduledTask.setForceExecution(forceExec);
        }
        asyncExecutor.execute((Runnable) task);
        log.info("手动触发任务执行, id={}, forceExec={}, params={}", id, forceExec, params);
        return dto;
    }
}
