package com.tieba.service;

import com.tieba.entity.UserConfig;
import com.tieba.repository.UserConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户配置服务
 */
@Service
@RequiredArgsConstructor
public class UserConfigService {
    
    private final UserConfigRepository userConfigRepository;
    
    /**
     * 获取配置
     */
    public UserConfig getConfig() {
        UserConfig config = userConfigRepository.findFirstBy();
        if (config == null) {
            config = new UserConfig();
            config.setId(1L);
            config.setCreatedTime(LocalDateTime.now());
            config.setUpdateTime(LocalDateTime.now());
            config = userConfigRepository.save(config);
        }
        return config;
    }
    
    /**
     * 更新配置
     */
    @Transactional
    public UserConfig updateConfig(UserConfig config) {
        config.setUpdateTime(LocalDateTime.now());
        return userConfigRepository.save(config);
    }
} 