package com.boot.scheduled.service;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.boot.scheduled.bean.TaskList;
import com.boot.scheduled.repository.TaskListRepository;

/**
 * 定时任务数据访问服务
 * <p>
 * 封装对 task_list 表的CRUD操作，提供事务保证。
 * </p>
 *
 * @author MiMoCode
 */
@Service
public class TaskListService {

    private final TaskListRepository taskListRepository;

    public TaskListService(TaskListRepository taskListRepository) {
        this.taskListRepository = taskListRepository;
    }

    /**
     * 保存或更新任务记录
     *
     * @param taskList 任务实体
     */
    @Transactional
    public void save(TaskList taskList) {
        taskListRepository.save(taskList);
    }

    /**
     * 按创建时间倒序查询全部任务
     *
     * @return 任务列表
     */
    public List<TaskList> findAll() {
        return taskListRepository.findAll(Sort.by(Sort.Direction.DESC, "createtime"));
    }

    /**
     * 根据条件分页查询任务
     *
     * @param spec     JPA动态查询条件
     * @param pageable 分页参数
     * @return 分页结果
     */
    public Page<TaskList> findAll(Specification<TaskList> spec, Pageable pageable) {
        return taskListRepository.findAll(spec, pageable);
    }

    /**
     * 根据ID查询任务
     *
     * @param id 任务主键ID
     * @return 任务Optional包装
     */
    public Optional<TaskList> findById(Integer id) {
        return taskListRepository.findById(id);
    }

    /**
     * 根据ID更新cron表达式（使用原生SQL直接更新）
     *
     * @param cron 新的cron表达式
     * @param id   任务主键ID
     */
    @Transactional
    public void updateCronById(String cron, Integer id) {
        taskListRepository.updateCronById(cron, id);
    }
}
