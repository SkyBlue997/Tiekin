package com.tieba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 贴吧签到程序入口
 */
@SpringBootApplication
@EnableScheduling
public class TiebaSignInApplication {

    public static void main(String[] args) {
        SpringApplication.run(TiebaSignInApplication.class, args);
    }
} 