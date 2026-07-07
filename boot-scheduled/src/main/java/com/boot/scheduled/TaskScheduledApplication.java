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

import lombok.extern.slf4j.Slf4j;

/**
 * 动态定时任务管理平台 - 启动类
 * <p>
 * 支持通过数据库配置定时任务的cron表达式，动态管理任务的启动、暂停、删除和修改。
 * 使用 dynamictp 动态线程池实现可监控的调度能力，支持在线Java代码编译执行。
 * </p>
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
