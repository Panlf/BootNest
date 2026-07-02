package com.boot.scheduled.bean;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 定时任务实体类
 * <p>
 * 对应数据库 task_list 表，存储定时任务的配置信息。
 * 包含cron表达式、任务类全名、状态等字段。
 * </p>
 *
 * @author MiMoCode
 */
@Entity
@Table(name = "task_list")
@Data
@ApiModel("定时任务")
public class TaskList {

    /** 主键ID，数据库自增 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "主键ID", required = false, example = "1")
    private Integer id;

    /** cron调度表达式 */
    @Column(name = "cron")
    @ApiModelProperty(value = "Cron表达式", required = true, example = "*/5 * * * * ?")
    private String cron;

    /** 任务类全限定名，需实现Runnable接口 */
    @Column(name = "clazz")
    @ApiModelProperty(value = "任务全类名", required = true, example = "com.boot.scheduled.job.MyRunnable")
    private String clazz;

    /**
     * 任务状态
     * <ul>
     *   <li>0 - 已删除</li>
     *   <li>1 - 运行中</li>
     *   <li>2 - 已暂停</li>
     * </ul>
     */
    @Column(name = "status")
    @ApiModelProperty(value = "任务状态: 0-删除, 1-启动, 2-停止", required = false, example = "1")
    private Integer status;

    /** 任务名称，用于展示和搜索 */
    @Column(name = "taskname")
    @ApiModelProperty(value = "任务名称", required = true, example = "任务一")
    private String taskname;

    /** 创建时间，新增任务时自动设置 */
    @Column(name = "createtime")
    @ApiModelProperty(value = "创建时间", required = false, example = "2024-01-01 12:00:00")
    private Date createtime;

    /**
     * 是否允许并发执行
     * <ul>
     *   <li>1 - 允许（上一次未完成时仍可执行）</li>
     *   <li>0 - 不允许（上一次未完成时跳过本次触发）</li>
     * </ul>
     */
    @Column(name = "allow_concurrent")
    @ApiModelProperty(value = "是否允许并发执行: 1-允许, 0-不允许", required = false, example = "0")
    private Integer allowConcurrent = 1;
}
