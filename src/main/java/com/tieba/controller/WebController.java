package com.tieba.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web控制器 - 处理页面路由
 */
@Controller
public class WebController {

    /**
     * 首页
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }
} 