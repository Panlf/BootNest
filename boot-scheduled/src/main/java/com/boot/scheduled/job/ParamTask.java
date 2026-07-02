package com.boot.scheduled.job;

import com.boot.scheduled.bean.TaskList;
import com.boot.scheduled.configuration.ApplicationContextProvider;
import com.boot.scheduled.service.TaskListService;
import lombok.extern.slf4j.Slf4j;

/**
 * 带参数的示例定时任务
 * <p>
 * 演示如何在定时任务中接收运行时参数。
 * 支持的参数：
 * <ul>
 *   <li><b>userId</b> - 用户ID，不传则默认为 "default_user"</li>
 *   <li><b>count</b> - 查询数量上限，不传则默认为 10</li>
 * </ul>
 * </p>
 *
 * <p>手动触发示例：</p>
 * <pre>
 * POST /api/task/trigger/params?id=1
 * {
 *   "userId": "10086",
 *   "count": "5"
 * }
 * </pre>
 *
 * @author MiMoCode
 */
@Slf4j
public class ParamTask extends BaseScheduledTask {

    private static final String DEFAULT_USER_ID = "default_user";
    private static final int DEFAULT_COUNT = 10;

    private final TaskListService taskListService;

    public ParamTask() {
        super("带参数的示例任务");
        this.taskListService = ApplicationContextProvider.getBean(TaskListService.class);
    }

    @Override
    protected void execute() {
        String userId = getParam("userId", DEFAULT_USER_ID);
        int count = getParamAsInt("count", DEFAULT_COUNT);

        log.info("[带参数任务] userId={}, count={}", userId, count);

        java.util.List<TaskList> list = taskListService.findAll();
        int resultSize = Math.min(list.size(), count);
        log.info("[带参数任务] 查询到 {} 条任务, 取前 {} 条", list.size(), resultSize);

        for (int i = 0; i < resultSize; i++) {
            TaskList task = list.get(i);
            log.info("[带参数任务] 任务[{}] - name={}, cron={}", i + 1, task.getTaskname(), task.getCron());
        }

        log.info("[带参数任务] 执行完毕, userId={}", userId);
    }
}
