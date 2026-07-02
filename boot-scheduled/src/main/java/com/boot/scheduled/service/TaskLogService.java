package com.boot.scheduled.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.boot.scheduled.bean.TaskLog;
import com.boot.scheduled.repository.TaskLogRepository;

/**
 * 任务执行日志服务
 *
 * @author MiMoCode
 */
@Service
public class TaskLogService {

    private final TaskLogRepository taskLogRepository;

    public TaskLogService(TaskLogRepository taskLogRepository) {
        this.taskLogRepository = taskLogRepository;
    }

    /**
     * 保存执行日志
     *
     * @param taskLog 日志记录
     */
    public void save(TaskLog taskLog) {
        taskLogRepository.save(taskLog);
    }

    /**
     * 根据任务ID查询执行日志
     *
     * @param taskId 任务ID
     * @return 日志列表
     */
    public List<TaskLog> findByTaskId(Integer taskId) {
        return taskLogRepository.findByTaskIdOrderByCreateTimeDesc(taskId);
    }

    /**
     * 查询所有执行日志
     *
     * @return 日志列表
     */
    public List<TaskLog> findAll() {
        return taskLogRepository.findAllByOrderByCreateTimeDesc();
    }
}
