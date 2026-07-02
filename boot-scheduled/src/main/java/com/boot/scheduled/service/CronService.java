package com.boot.scheduled.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.boot.scheduled.bean.TaskList;
import com.boot.scheduled.common.BusinessException;
import com.boot.scheduled.container.MapContainer;

import lombok.extern.slf4j.Slf4j;

/**
 * 定时任务核心业务服务
 * <p>
 * 负责定时任务的增删改查，以及与 MapContainer 的协调管理。
 * 所有任务状态变更操作都会同步更新数据库和内存容器。
 * </p>
 *
 * @author MiMoCode
 */
@Service
@Slf4j
public class CronService {

    private final TaskListService taskListService;
    private final MapContainer mapContainer;

    public CronService(TaskListService taskListService, MapContainer mapContainer) {
        this.taskListService = taskListService;
        this.mapContainer = mapContainer;
    }

    /**
     * 新增定时任务并启动调度
     *
     * @param taskList 任务信息
     */
    public void addTaskList(TaskList taskList) {
        taskList.setCreatetime(new Date());
        taskList.setStatus(1);
        taskListService.save(taskList);
        mapContainer.putMap(taskList);
        log.info("新增定时任务成功, id={}, name={}, cron={}", taskList.getId(), taskList.getTaskname(), taskList.getCron());
    }

    /**
     * 暂停定时任务
     *
     * @param id 任务主键ID
     */
    public void cancelTaskList(Integer id) {
        TaskList taskList = getTaskListById(id);
        if (taskList == null) {
            throw new BusinessException("任务不存在, id=" + id);
        }
        taskList.setStatus(2);
        taskListService.save(taskList);
        mapContainer.cancelMap(id);
        log.info("暂停定时任务成功, id={}, name={}", id, taskList.getTaskname());
    }

    /**
     * 逻辑删除定时任务
     *
     * @param id 任务主键ID
     */
    public void deleteTaskList(Integer id) {
        TaskList taskList = getTaskListById(id);
        if (taskList == null) {
            throw new BusinessException("任务不存在, id=" + id);
        }
        taskList.setStatus(0);
        taskListService.save(taskList);
        mapContainer.deleteMap(id);
        log.info("删除定时任务成功, id={}, name={}", id, taskList.getTaskname());
    }

    /**
     * 重新启动定时任务
     *
     * @param id 任务主键ID
     */
    public void restartTaskList(Integer id) {
        TaskList taskList = getTaskListById(id);
        if (taskList == null) {
            throw new BusinessException("任务不存在, id=" + id);
        }
        taskList.setStatus(1);
        taskListService.save(taskList);
        mapContainer.restartMap(taskList);
        log.info("重启定时任务成功, id={}, name={}", id, taskList.getTaskname());
    }

    /**
     * 修改定时任务的cron表达式
     * <p>
     * 先暂停旧任务，再用新cron表达式重新调度，最后同步更新数据库。
     * </p>
     *
     * @param id   任务主键ID
     * @param cron 新的cron表达式
     */
    public void updateTaskList(Integer id, String cron) {
        TaskList taskList = getTaskListById(id);
        if (taskList == null) {
            throw new BusinessException("任务不存在, id=" + id);
        }
        taskList.setCron(cron);

        // 暂停旧任务，重新调度
        mapContainer.cancelMap(id);
        mapContainer.putMap(taskList);

        // 同步更新数据库
        taskListService.updateCronById(cron, id);
        log.info("修改定时任务成功, id={}, newCron={}", id, cron);
    }

    /**
     * 扫描指定包下所有类，返回类列表
     *
     * @param packageName 包名（如 com.boot.scheduled.job）
     * @return 该包下所有类的Class对象列表
     */
    public List<Class<?>> getJobClass(String packageName) {
        List<Class<?>> list = new ArrayList<>();
        try {
            Enumeration<URL> iterator = Thread.currentThread().getContextClassLoader()
                    .getResources(packageName.replace(".", "/"));
            while (iterator.hasMoreElements()) {
                URL url = iterator.nextElement();
                if ("file".equals(url.getProtocol())) {
                    File file = new File(url.getPath());
                    if (file.isDirectory()) {
                        File[] files = file.listFiles();
                        if (files == null) {
                            continue;
                        }
                        for (File f : files) {
                            String className = f.getName();
                            if (!className.endsWith(".class")) {
                                continue;
                            }
                            className = className.substring(0, className.lastIndexOf("."));
                            Class<?> clazz = Thread.currentThread().getContextClassLoader()
                                    .loadClass(packageName + "." + className);
                            list.add(clazz);
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            log.error("扫描包下类失败, packageName={}", packageName, e);
        }
        return list;
    }

    /**
     * 根据ID获取任务信息
     *
     * @param id 任务主键ID
     * @return 任务信息，不存在时返回null
     */
    public TaskList getTaskListById(Integer id) {
        Optional<TaskList> optTask = taskListService.findById(id);
        return optTask.orElse(null);
    }

    /**
     * 根据任务名称分页查询
     *
     * @param pageSize   每页数量
     * @param pageNumber 页码（从1开始）
     * @param taskname   任务名称（模糊搜索，可为null）
     * @return 分页结果
     */
    public Page<TaskList> findPageByName(Integer pageSize, Integer pageNumber, String taskname) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        Specification<TaskList> spec = (Root<TaskList> root, CriteriaQuery<?> criteria,
                                        CriteriaBuilder criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (taskname != null && !taskname.trim().isEmpty()) {
                // 转义LIKE通配符，防止注入
                String escaped = taskname.replace("\\", "\\\\")
                        .replace("%", "\\%")
                        .replace("_", "\\_");
                predicates.add(criteriaBuilder.like(
                        root.get("taskname").as(String.class),
                        "%" + escaped + "%",
                        '\\'));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return taskListService.findAll(spec, pageable);
    }
}
