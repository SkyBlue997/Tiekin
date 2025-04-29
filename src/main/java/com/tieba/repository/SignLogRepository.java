package com.tieba.repository;

import com.tieba.entity.SignLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 签到日志仓库
 */
@Repository
public interface SignLogRepository extends JpaRepository<SignLog, Long> {
    
    /**
     * 按照签到日期倒序获取签到日志
     */
    List<SignLog> findAllByOrderBySignDateDesc();
} 