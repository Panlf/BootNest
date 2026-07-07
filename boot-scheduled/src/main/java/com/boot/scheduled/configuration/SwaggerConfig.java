package com.boot.scheduled.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("动态定时任务管理平台")
                        .description("基于Spring Boot和dynamictp的动态定时任务管理系统，"
                                + "支持任务的动态新增、修改、暂停、删除和重启。")
                        .version("1.0.0"));
    }
}
