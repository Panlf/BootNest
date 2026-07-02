package com.boot.scheduled.job;

import com.boot.scheduled.bean.TaskList;
import com.boot.scheduled.configuration.ApplicationContextProvider;
import com.boot.scheduled.service.TaskListService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 示例定时任务 - 任务2（需要访问Spring Bean的场景）
 * <p>
 * 继承 {@link BaseScheduledTask}，在构造函数中通过 ApplicationContextProvider 获取 Spring Bean。
 * 演示非Spring管理的任务类如何集成Spring生态。
 * </p>
 *
 * @author MiMoCode
 */
@Slf4j
public class MyRunnable2 extends BaseScheduledTask {

    private final TaskListService taskListService;

    public MyRunnable2() {
        super("示例任务-2");
        this.taskListService = ApplicationContextProvider.getBean(TaskListService.class);
    }

    @Override
    protected void execute() {
        List<TaskList> list = taskListService.findAll();
        log.info("当前数据库中的定时任务数量: {}", list.size());
    }
}
