package com.tieba.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 签到日志实体
 */
@Data
@Entity
@Table(name = "sign_log")
public class SignLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 签到日期
     */
    private LocalDateTime signDate;
    
    /**
     * 关注贴吧总数
     */
    private Integer totalCount;
    
    /**
     * 签到成功数量
     */
    private Integer successCount;
    
    /**
     * 签到失败数量
     */
    private Integer failCount;
    
    /**
     * 签到状态（成功、失败）
     */
    private String status;
    
    /**
     * 详细日志
     */
    @Column(length = 4096)
    private String detail;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
} 