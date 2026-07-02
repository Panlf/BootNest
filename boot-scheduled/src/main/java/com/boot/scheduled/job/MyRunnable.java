package com.boot.scheduled.job;

import lombok.extern.slf4j.Slf4j;

/**
 * 示例定时任务 - 任务1
 * <p>
 * 继承 {@link BaseScheduledTask}，只需实现 execute() 方法。
 * 基类自动记录启动时间、结束时间、执行状态和异常信息。
 * </p>
 *
 * @author MiMoCode
 */
@Slf4j
public class MyRunnable extends BaseScheduledTask {

    public MyRunnable() {
        super("示例任务-1");
    }

    @Override
    protected void execute() {
        log.info("执行示例任务1的业务逻辑");
    }
}
