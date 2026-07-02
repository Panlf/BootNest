package com.boot.scheduled.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.boot.scheduled.bean.TaskLog;
import com.boot.scheduled.common.BusinessException;
import com.boot.scheduled.common.Result;
import com.boot.scheduled.container.MapContainer;
import com.boot.scheduled.dto.TaskListDto;
import com.boot.scheduled.service.JavaCodeCronService;
import com.boot.scheduled.service.TaskLogService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import com.boot.scheduled.bean.TaskList;
import com.boot.scheduled.service.CronService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 定时任务管理控制器
 * <p>
 * 提供定时任务的增删改查、暂停、重启等REST接口。
 * 所有修改类操作使用POST/PUT/DELETE，查询使用GET。
 * </p>
 *
 * @author MiMoCode
 */
@Api(tags = "定时任务管理")
@RestController
@RequestMapping("/api/task")
public class CronController {

    private final CronService cronService;
    private final JavaCodeCronService javaCodeCronService;
    private final MapContainer mapContainer;
    private final TaskLogService taskLogService;

    public CronController(CronService cronService, JavaCodeCronService javaCodeCronService,
                          MapContainer mapContainer, TaskLogService taskLogService) {
        this.cronService = cronService;
        this.javaCodeCronService = javaCodeCronService;
        this.mapContainer = mapContainer;
        this.taskLogService = taskLogService;
    }

    /**
     * 排除不需要前端传入的字段，createtime由后端自动设置
     */
    @InitBinder("taskList")
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("createtime");
    }

    /**
     * 新增定时任务
     *
     * @param taskList 定时任务信息（cron表达式、任务类名、任务名等）
     * @return 操作结果
     */
    @ApiOperation("新增定时任务")
    @PostMapping("/add")
    public Result<Void> addCron(TaskList taskList) {
        cronService.addTaskList(taskList);
        return Result.success();
    }

    /**
     * 修改定时任务的cron表达式
     *
     * @param taskList 包含id和新cron表达式的任务信息
     * @return 操作结果
     */
    @ApiOperation("修改定时任务cron表达式")
    @PutMapping("/update")
    public Result<Void> updateCron(TaskList taskList) {
        cronService.updateTaskList(taskList.getId(), taskList.getCron());
        return Result.success();
    }

    /**
     * 暂停定时任务
     *
     * @param id 任务主键ID
     * @return 操作结果
     */
    @ApiOperation("暂停定时任务")
    @PostMapping("/stop")
    public Result<Void> stopCron(@RequestParam Integer id) {
        cronService.cancelTaskList(id);
        return Result.success();
    }

    /**
     * 删除定时任务（逻辑删除）
     *
     * @param id 任务主键ID
     * @return 操作结果
     */
    @ApiOperation("删除定时任务")
    @DeleteMapping("/delete")
    public Result<Void> deleteCron(@RequestParam Integer id) {
        cronService.deleteTaskList(id);
        return Result.success();
    }

    /**
     * 重新启动定时任务
     *
     * @param id 任务主键ID
     * @return 操作结果
     */
    @ApiOperation("重新启动定时任务")
    @PostMapping("/restart")
    public Result<Void> restartCron(@RequestParam Integer id) {
        cronService.restartTaskList(id);
        return Result.success();
    }

    /**
     * 手动触发执行一次任务（无参数）
     *
     * @param id        任务主键ID
     * @param forceExec 是否强制执行（true=即使上一次未完成也执行，默认false）
     * @return 操作结果
     */
    @ApiOperation("手动触发执行一次任务")
    @PostMapping("/trigger")
    public Result<Void> triggerTask(@RequestParam Integer id,
                                    @RequestParam(required = false, defaultValue = "false") Boolean forceExec) {
        mapContainer.triggerTask(id, new java.util.HashMap<>(), forceExec);
        return Result.success();
    }

    /**
     * 手动触发执行一次任务（带参数）
     * <p>
     * forceExec=true 时，忽略 allowConcurrent 设置，强制执行一次。
     * forceExec=false（默认）时，如果任务正在执行且 allowConcurrent=false，本次触发被跳过。
     * </p>
     *
     * @param id        任务主键ID
     * @param forceExec 是否强制执行
     * @param params    参数Map，key为参数名，value为参数值
     * @return 操作结果
     */
    @ApiOperation("手动触发执行一次任务（带参数）")
    @PostMapping("/trigger/params")
    public Result<Void> triggerTaskWithParams(@RequestParam Integer id,
                                              @RequestParam(required = false, defaultValue = "false") Boolean forceExec,
                                              @RequestBody(required = false) java.util.Map<String, String> params) {
        mapContainer.triggerTask(id, params, forceExec);
        return Result.success();
    }

    /**
     * 获取job包下所有可调度任务类的全类名
     *
     * @return 类全名列表
     */
    @ApiOperation("获取所有job包下类的全类名")
    @GetMapping("/job-classes")
    public Result<List<String>> getJobClasses() {
        List<Class<?>> list = cronService.getJobClass("com.boot.scheduled.job");
        List<String> classNames = list.stream()
                .map(Class::getName)
                .collect(Collectors.toList());
        return Result.success(classNames);
    }

    /**
     * 分页查询定时任务列表
     *
     * @param taskname   任务名称（模糊搜索，可选）
     * @param pageNumber 页码（从1开始，默认1）
     * @param pageSize   每页数量（默认10）
     * @return 分页任务列表
     */
    @ApiOperation("分页获取定时任务列表")
    @GetMapping("/list")
    public Result<Page<TaskList>> getTaskList(
            @RequestParam(required = false) String taskname,
            @RequestParam(required = false, defaultValue = "1") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        return Result.success(cronService.findPageByName(pageSize, pageNumber, taskname));
    }

    /**
     * 查询指定任务的运行状态
     *
     * @param id 任务主键ID
     * @return 任务运行状态信息（启动时间、结束时间、是否运行中等）
     */
    @ApiOperation("查询任务运行状态")
    @GetMapping("/status")
    public Result<TaskListDto> getTaskStatus(@RequestParam Integer id) {
        TaskListDto dto = mapContainer.getById(id);
        if (dto == null) {
            throw new BusinessException("任务未在运行中, id=" + id);
        }
        dto.syncStatusFromTask();
        return Result.success(dto);
    }

    /**
     * 查询所有运行中任务的状态概览
     *
     * @return 所有任务的状态摘要列表
     */
    @ApiOperation("查询所有任务运行状态")
    @GetMapping("/status/all")
    public Result<List<TaskListDto>> getAllTaskStatus() {
        mapContainer.getMapContainer().values().forEach(TaskListDto::syncStatusFromTask);
        return Result.success(new java.util.ArrayList<>(mapContainer.getMapContainer().values()));
    }

    /**
     * 查询指定任务的执行日志
     *
     * @param taskId 任务ID
     * @return 执行日志列表（按时间倒序）
     */
    @ApiOperation("查询任务执行日志")
    @GetMapping("/log")
    public Result<List<TaskLog>> getTaskLogs(@RequestParam Integer taskId) {
        return Result.success(taskLogService.findByTaskId(taskId));
    }

    /**
     * 查询所有任务的执行日志
     *
     * @return 全部执行日志列表（按时间倒序）
     */
    @ApiOperation("查询所有任务执行日志")
    @GetMapping("/log/all")
    public Result<List<TaskLog>> getAllTaskLogs() {
        return Result.success(taskLogService.findAll());
    }

    /**
     * 通过在线Java代码启动定时任务
     * <p>
     * 注意：不支持引入第三方Jar包。功能有限，不集成到MapContainer管理。
     * </p>
     *
     * @param cron     cron表达式
     * @param javaCode 实现Runnable接口的Java代码
     * @return 操作结果
     */
    @ApiOperation("启动在线代码的定时任务（不支持引入第三方Jar）")
    @PostMapping("/start-code")
    public Result<Void> startCronWithJavaCode(@RequestParam String cron, @RequestParam String javaCode) {
        try {
            javaCodeCronService.startJavaCodeTask(javaCode, cron);
            return Result.success();
        } catch (Exception e) {
            throw new BusinessException("在线代码编译执行失败: " + e.getMessage());
        }
    }
}
