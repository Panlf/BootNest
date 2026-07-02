package com.boot.scheduled.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.boot.scheduled.bean.TaskList;

/**
 * 定时任务数据访问接口
 * <p>
 * 继承 JpaRepository 提供标准CRUD，JpaSpecificationExecutor 支持动态条件查询。
 * </p>
 *
 * @author MiMoCode
 */
public interface TaskListRepository extends JpaRepository<TaskList, Integer>, JpaSpecificationExecutor<TaskList> {

    /**
     * 根据状态查询任务列表
     *
     * @param status 任务状态: 0-删除, 1-启动, 2-停止
     * @return 任务列表
     */
    List<TaskList> findByStatus(Integer status);

    /**
     * 根据ID更新cron表达式
     *
     * @param cron 新的cron表达式
     * @param id   任务主键ID
     */
    @Modifying
    @Query(value = "update task_list set cron=?1 where id=?2", nativeQuery = true)
    void updateCronById(String cron, Integer id);
}
