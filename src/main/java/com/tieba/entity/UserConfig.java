package com.tieba.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * 用户配置实体
 */
@Data
@Entity
@Table(name = "user_config")
public class UserConfig {
    
    @Id
    private Long id;
    
    /**
     * 贴吧BDUSS
     */
    private String bduss;
    
    /**
     * Bark推送服务器地址
     */
    private String barkServer;
    
    /**
     * Bark推送Key
     */
    private String barkKey;
    
    /**
     * 是否启用推送
     */
    private Boolean pushEnabled = false;
    
    /**
     * 上次签到时间
     */
    private LocalDateTime lastSignTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 