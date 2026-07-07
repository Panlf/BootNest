package com.boot.scheduled.bean;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 定时任务执行日志实体
 * <p>
 * 记录每次定时任务的执行情况，包括启动时间、结束时间、执行状态和异常信息。
 * </p>
 *
 * @author MiMoCode
 */
@Entity
@Table(name = "task_log")
@Data
@Schema(description = "任务执行日志")
public class TaskLog {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "主键ID", example = "1")
    private Long id;

    /** 关联任务ID（对应task_list表的id） */
    @Column(name = "task_id")
    @Schema(description = "关联任务ID", example = "1")
    private Integer taskId;

    /** 任务名称 */
    @Column(name = "task_name")
    @Schema(description = "任务名称", example = "示例任务-1")
    private String taskName;

    /** 任务类全限定名 */
    @Column(name = "task_class")
    @Schema(description = "任务类全名", example = "com.boot.scheduled.job.MyRunnable")
    private String taskClass;

    /** 执行开始时间 */
    @Column(name = "start_time")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    /** 执行结束时间 */
    @Column(name = "end_time")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    /**
     * 执行状态
     * <ul>
     *   <li>1 - 成功</li>
     *   <li>0 - 失败</li>
     * </ul>
     */
    @Column(name = "status")
    @Schema(description = "执行状态: 1-成功, 0-失败", example = "1")
    private Integer status;

    /** 错误信息（成功时为null） */
    @Column(name = "error_msg", columnDefinition = "TEXT")
    @Schema(description = "错误信息")
    private String errorMsg;

    /** 执行线程名 */
    @Column(name = "thread_name")
    @Schema(description = "执行线程", example = "schedule-task-1")
    private String threadName;

    /** 记录创建时间 */
    @Column(name = "create_time")
    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;
}
