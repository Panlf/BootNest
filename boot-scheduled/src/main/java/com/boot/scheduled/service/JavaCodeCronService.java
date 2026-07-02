package com.boot.scheduled.service;

import com.boot.scheduled.common.BusinessException;
import com.boot.scheduled.utils.ParseJavaCode;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

/**
 * 在线Java代码定时任务服务
 * <p>
 * 支持通过在线提交Java源码动态编译并启动定时任务。
 * 基于 javax.tools.JavaCompiler 实现运行时编译。
 * 注意：不支持引入第三方Jar包。
 * </p>
 *
 * @author MiMoCode
 */
@Service
public class JavaCodeCronService {

    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private final ParseJavaCode compiler = new ParseJavaCode();

    public JavaCodeCronService(ThreadPoolTaskScheduler threadPoolTaskScheduler) {
        this.threadPoolTaskScheduler = threadPoolTaskScheduler;
    }

    /**
     * 编译Java代码并启动定时任务
     *
     * @param code 实现Runnable接口的Java源代码
     * @param cron cron调度表达式
     * @throws BusinessException 编译失败或实例化失败时抛出
     */
    public void startJavaCodeTask(String code, String cron) {
        try {
            Class<?> cla = compiler.compile(code);
            Runnable obj = (Runnable) cla.getDeclaredConstructor().newInstance();
            threadPoolTaskScheduler.schedule(obj, new CronTrigger(cron));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("在线代码编译执行失败: " + e.getMessage());
        }
    }
}
