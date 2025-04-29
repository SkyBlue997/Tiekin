package com.tieba.repository;

import com.tieba.entity.UserConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 用户配置仓库
 */
@Repository
public interface UserConfigRepository extends JpaRepository<UserConfig, Long> {
    
    /**
     * 获取配置信息
     */
    UserConfig findFirstBy();
} 