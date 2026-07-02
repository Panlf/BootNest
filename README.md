# BootNest

基于 Spring Boot 2.7.18 的多功能演示项目集合，包含定时任务管理、请求拦截、高性能队列、MyBatis插件等多个独立子模块。

## 项目结构

```
BootNest/
├── boot-scheduled/      # 动态定时任务管理
├── boot-intercept/      # Aspect、Filter、Interceptor 演示
├── boot-disruptor/      # LMAX Disruptor 高性能队列集成
├── boot-mybatis-plugin/ # MyBatis 插件（全表扫描拦截、SQL打印）
└── pom.xml              # 父POM
```

## 技术栈

- **JDK**: 8+
- **Spring Boot**: 2.7.18
- **Lombok**: 1.18.16
- **构建工具**: Maven

## 模块说明

### 1. boot-scheduled - 动态定时任务管理

动态定时任务管理模块，支持运行时创建、修改、删除定时任务，无需重启应用。

**功能特性**：
- 基于 Spring `@Scheduled` 注解的任务调度
- 支持 Cron 表达式的动态任务配置
- 任务执行日志记录
- Swagger API 文档支持
- 任务参数化执行

**核心类**：
- `CronController` - 任务管理 REST API
- `TaskListService` - 任务列表 CRUD 服务
- `CronService` - Cron 表达式调度服务
- `TaskLogService` - 任务日志记录服务

### 2. boot-intercept - 请求拦截演示

演示 Spring 中 Filter、Interceptor、Aspect 三种请求处理机制的实现方式。

**功能特性**：
- **Filter**: `RequestTraceFilter` - 请求追踪过滤器
- **Interceptor**: `TokenValidationInterceptor` - Token 校验拦截器
- **Interceptor**: `RequestTimingInterceptor` - 请求耗时统计
- **Aspect**: `RequestLogAspect` - 请求日志切面
- **Aspect**: `RateLimitAspect` - 基于注解的限流切面
- 基于 Guava Cache 的本地缓存配置
- 统一异常处理

**核心注解**：
- `@RateLimit` - 接口限流注解

### 3. boot-disruptor - 高性能队列演示

集成 LMAX Disruptor 框架的高性能消息队列示例。

**功能特性**：
- 基于 Disruptor 的异步消息处理
- 环形缓冲区队列管理
- 消息消费者处理

**核心类**：
- `QueueManager` - Disruptor 队列管理器
- `MessageConsumer` - 消息消费者
- `DisruptorMessageService` - 消息服务接口

### 4. boot-mybatis-plugin - MyBatis 插件

MyBatis 拦截器插件示例，提供全表扫描拦截和 SQL 打印功能。

**功能特性**：
- **全表扫描拦截**: `MyBatisFullTableSearchInterceptor` - 拦截无 WHERE 条件的全表操作
- **SQL 打印**: `MybatisSqlStatementInterceptor` - 打印带参数的完整 SQL
- **SQL 执行监控**: `MybatisSqlExecutorInterceptor` - SQL 执行耗时统计
- 自定义注解 `@EnableTableScan` 启用全表扫描检测

## 快速开始

### 环境要求

- JDK 8+
- Maven 3.6+
- MySQL（boot-mybatis-plugin 模块需要）

### 构建项目

```bash
mvn clean install
```

### 运行模块

```bash
# 运行 boot-scheduled 模块
cd boot-scheduled
mvn spring-boot:run

# 运行 boot-intercept 模块
cd boot-intercept
mvn spring-boot:run

# 运行 boot-disruptor 模块
cd boot-disruptor
mvn spring-boot:run

# 运行 boot-mybatis-plugin 模块
cd boot-mybatis-plugin
mvn spring-boot:run
```

## 许可证

[MIT License](LICENSE)