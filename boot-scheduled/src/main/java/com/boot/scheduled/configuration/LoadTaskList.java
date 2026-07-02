package com.boot.scheduled.configuration;

import com.boot.scheduled.bean.TaskList;
import com.boot.scheduled.container.MapContainer;
import com.boot.scheduled.repository.TaskListRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 应用启动后自动加载数据库中的活跃定时任务
 * <p>
 * 在Spring容器初始化完成后，从数据库查询状态为"启动"(status=1)的任务，
 * 逐一注册到 MapContainer 中开始调度。
 * </p>
 *
 * @author MiMoCode
 */
@Component
@Order(1)
@Slf4j
public class LoadTaskList implements CommandLineRunner {

    private final TaskListRepository taskListRepository;
    private final MapContainer mapContainer;

    public LoadTaskList(TaskListRepository taskListRepository, MapContainer mapContainer) {
        this.taskListRepository = taskListRepository;
        this.mapContainer = mapContainer;
    }

    @Override
    public void run(String... args) {
        log.info("开始加载数据库中的活跃定时任务...");
        List<TaskList> activeTasks = taskListRepository.findByStatus(1);

        if (activeTasks == null || activeTasks.isEmpty()) {
            log.info("没有需要加载的活跃定时任务");
            return;
        }

        int loadedCount = 0;
        for (TaskList task : activeTasks) {
            try {
                mapContainer.putMap(task);
                loadedCount++;
            } catch (Exception e) {
                log.error("加载定时任务失败, id={}, class={}", task.getId(), task.getClazz(), e);
            }
        }
        log.info("定时任务加载完成, 成功: {}/{}", loadedCount, activeTasks.size());
    }
}
