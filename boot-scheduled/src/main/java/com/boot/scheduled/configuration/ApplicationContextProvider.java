package com.boot.scheduled.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring ApplicationContext 持有者
 * <p>
 * 提供静态方法获取Spring容器中的Bean实例。
 * 主要用于非Spring管理的对象（如动态编译的Job类）需要获取Spring Bean的场景。
 * </p>
 *
 * @author MiMoCode
 */
@Slf4j
@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextProvider.applicationContext = applicationContext;
        log.info("ApplicationContext 已注入");
    }

    /**
     * 根据名称获取Bean
     *
     * @param name Bean名称
     * @return Bean实例
     */
    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    /**
     * 根据类型获取Bean
     *
     * @param clazz Bean类型
     * @return Bean实例
     */
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    /**
     * 根据名称和类型获取Bean
     *
     * @param name  Bean名称
     * @param clazz Bean类型
     * @return Bean实例
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }
}
