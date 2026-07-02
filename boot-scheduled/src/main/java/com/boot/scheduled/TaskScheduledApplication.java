package com.boot.scheduled;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import lombok.extern.slf4j.Slf4j;

/**
 * 动态定时任务管理平台 - 启动类
 * <p>
 * 支持通过数据库配置定时任务的cron表达式，动态管理任务的启动、暂停、删除和修改。
 * 使用 ThreadPoolTaskScheduler 作为任务调度器，支持在线Java代码编译执行。
 * </p>
 *
 * @author MiMoCode
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
@Slf4j
@EnableAsync
@EnableScheduling
public class TaskScheduledApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskScheduledApplication.class, args);
    }

    /**
     * 线程池任务调度器 Bean
     * <p>
     * 用于管理所有定时任务的调度执行，支持并发执行多个任务。
     * 关闭时等待任务完成，超时时间为60秒。
     * </p>
     *
     * @return ThreadPoolTaskScheduler 实例
     */
    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(20);
        scheduler.setThreadNamePrefix("schedule-task-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        log.info("ThreadPoolTaskScheduler 已初始化, 核心线程数: 20");
        return scheduler;
    }

    /**
     * Tomcat连接器自定义配置
     * <p>
     * 允许URL中包含特殊字符（如 |{}[]\），用于支持cron表达式作为URL参数。
     * </p>
     *
     * @return ConfigurableServletWebServerFactory 实例
     */
    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers((TomcatConnectorCustomizer) connector ->
                connector.setProperty("relaxedQueryChars", "|{}[]\\"));
        return factory;
    }
}
