package com.boot.scheduled.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.boot.scheduled.bean.TaskLog;

/**
 * 任务执行日志数据访问接口
 *
 * @author MiMoCode
 */
public interface TaskLogRepository extends JpaRepository<TaskLog, Long> {

    /**
     * 根据任务ID查询执行日志，按创建时间倒序
     *
     * @param taskId 任务ID
     * @return 日志列表
     */
    List<TaskLog> findByTaskIdOrderByCreateTimeDesc(Integer taskId);

    /**
     * 查询所有执行日志，按创建时间倒序
     *
     * @return 日志列表
     */
    List<TaskLog> findAllByOrderByCreateTimeDesc();
}
