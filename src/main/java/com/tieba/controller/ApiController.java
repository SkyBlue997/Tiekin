package com.tieba.controller;

import com.tieba.entity.SignLog;
import com.tieba.entity.UserConfig;
import com.tieba.repository.SignLogRepository;
import com.tieba.service.TiebaSignService;
import com.tieba.service.UserConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final UserConfigService userConfigService;
    private final TiebaSignService tiebaSignService;
    private final SignLogRepository signLogRepository;

    /**
     * 获取配置
     */
    @GetMapping("/config")
    public ResponseEntity<UserConfig> getConfig() {
        return ResponseEntity.ok(userConfigService.getConfig());
    }

    /**
     * 更新配置
     */
    @PostMapping("/config")
    public ResponseEntity<UserConfig> updateConfig(@RequestBody UserConfig config) {
        // 确保ID不变
        UserConfig existingConfig = userConfigService.getConfig();
        config.setId(existingConfig.getId());
        config.setCreatedTime(existingConfig.getCreatedTime());
        
        return ResponseEntity.ok(userConfigService.updateConfig(config));
    }

    /**
     * 手动执行签到
     */
    @PostMapping("/sign")
    public ResponseEntity<Map<String, Object>> manualSign() {
        UserConfig config = userConfigService.getConfig();
        SignLog result = tiebaSignService.doSignIn(config);
        
        Map<String, Object> response = new HashMap<>();
        if (result != null) {
            response.put("success", true);
            response.put("message", "签到执行完成");
            response.put("data", result);
        } else {
            response.put("success", false);
            response.put("message", "签到失败，请检查BDUSS配置");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取签到日志列表
     */
    @GetMapping("/logs")
    public ResponseEntity<List<SignLog>> getLogs() {
        return ResponseEntity.ok(signLogRepository.findAllByOrderBySignDateDesc());
    }

    /**
     * 获取单条日志详情
     */
    @GetMapping("/logs/{id}")
    public ResponseEntity<SignLog> getLogDetail(@PathVariable Long id) {
        return signLogRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
} 