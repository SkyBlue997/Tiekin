package com.tieba.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tieba.entity.SignLog;
import com.tieba.entity.UserConfig;
import com.tieba.repository.SignLogRepository;
import com.tieba.util.Encryption;
import com.tieba.util.HttpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 贴吧签到服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TiebaSignService {

    private final UserConfigService userConfigService;
    private final SignLogRepository signLogRepository;

    // 获取用户所有关注贴吧
    private static final String LIKE_URL = "https://tieba.baidu.com/mo/q/newmoindex";
    // 获取用户的tbs
    private static final String TBS_URL = "http://tieba.baidu.com/dc/common/tbs";
    // 贴吧签到接口
    private static final String SIGN_URL = "http://c.tieba.baidu.com/c/c/forum/sign";

    /**
     * 每天凌晨2点执行签到
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledSign() {
        UserConfig config = userConfigService.getConfig();
        if (!StringUtils.hasText(config.getBduss())) {
            log.warn("未配置BDUSS，跳过签到");
            return;
        }
        
        doSignIn(config);
    }

    /**
     * 手动执行签到
     */
    public SignLog doSignIn(UserConfig config) {
        if (!StringUtils.hasText(config.getBduss())) {
            log.warn("BDUSS为空，无法执行签到");
            return null;
        }

        // 存储签到日志
        SignLog signLog = new SignLog();
        signLog.setSignDate(LocalDateTime.now());
        signLog.setCreatedTime(LocalDateTime.now());
        
        List<String> success = new ArrayList<>();
        Set<String> failed = new HashSet<>();
        List<String> invalid = new ArrayList<>();
        
        try {
            // 设置请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Cookie", "BDUSS=" + config.getBduss());
            headers.put("Content-Type", "application/x-www-form-urlencoded");
            
            // 获取TBS
            String tbs = "";
            JSONObject jsonObject = HttpUtil.doGet(TBS_URL, headers);
            if ("1".equals(jsonObject.getString("is_login"))) {
                log.info("获取TBS成功");
                tbs = jsonObject.getString("tbs");
            } else {
                log.warn("获取TBS失败，可能是BDUSS已过期");
                signLog.setStatus("失败");
                signLog.setDetail("获取TBS失败，可能是BDUSS已过期");
                return signLogRepository.save(signLog);
            }

            // 获取关注的贴吧列表
            jsonObject = HttpUtil.doGet(LIKE_URL, headers);
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("like_forum");
            
            // 记录贴吧总数
            int followNum = jsonArray.size();
            signLog.setTotalCount(followNum);
            
            // 获取需要签到的贴吧
            List<String> needSignIn = new ArrayList<>();
            for (Object array : jsonArray) {
                String tiebaName = ((JSONObject) array).getString("forum_name");
                if ("0".equals(((JSONObject) array).getString("is_sign"))) {
                    // 添加到待签到列表
                    needSignIn.add(tiebaName.replace("+", "%2B"));
                } else {
                    // 已经签到的贴吧
                    success.add(tiebaName);
                }
            }

            // 开始签到
            int maxRetry = 5; // 最大重试次数
            while (!needSignIn.isEmpty() && maxRetry > 0) {
                log.info("第 {} 轮签到开始，还剩 {} 个贴吧需要签到", 6 - maxRetry, needSignIn.size());
                
                Iterator<String> iterator = needSignIn.iterator();
                while (iterator.hasNext()) {
                    String encodedName = iterator.next();
                    String tiebaName = encodedName.replace("%2B", "+");
                    
                    // 构造签到请求
                    String body = "kw=" + encodedName + "&tbs=" + tbs + "&sign=" + 
                            Encryption.enCodeMd5("kw=" + tiebaName + "tbs=" + tbs + "tiebaclient!!!");
                    
                    // 发送签到请求
                    JSONObject result = HttpUtil.doPost(SIGN_URL, body, headers);
                    
                    // 随机休眠，避免请求过快
                    int sleepTime = new Random().nextInt(200) + 300;
                    TimeUnit.MILLISECONDS.sleep(sleepTime);
                    
                    if ("0".equals(result.getString("error_code"))) {
                        iterator.remove();
                        success.add(tiebaName);
                        failed.remove(tiebaName);
                        log.info("{}：签到成功", tiebaName);
                    } else {
                        failed.add(tiebaName);
                        log.warn("{}：签到失败 - {}", tiebaName, result.getString("error_msg"));
                    }
                }
                
                if (!needSignIn.isEmpty()) {
                    // 每轮签到间隔30秒，避免触发风控
                    TimeUnit.SECONDS.sleep(30);
                    // 重新获取TBS
                    jsonObject = HttpUtil.doGet(TBS_URL, headers);
                    if ("1".equals(jsonObject.getString("is_login"))) {
                        tbs = jsonObject.getString("tbs");
                    }
                }
                
                maxRetry--;
            }
            
            // 更新签到日志
            signLog.setSuccessCount(success.size());
            signLog.setFailCount(failed.size());
            signLog.setStatus(failed.isEmpty() ? "成功" : "部分成功");
            
            // 构建详细日志
            StringBuilder detail = new StringBuilder();
            detail.append("签到结果：\n");
            detail.append("总计：").append(followNum).append(" 个贴吧\n");
            detail.append("成功：").append(success.size()).append(" 个\n");
            detail.append("失败：").append(failed.size()).append(" 个\n");
            
            if (!failed.isEmpty()) {
                detail.append("\n失败贴吧列表：\n");
                for (String name : failed) {
                    detail.append(name).append("\n");
                }
            }
            
            signLog.setDetail(detail.toString());
            
            // 发送推送通知
            if (config.getPushEnabled() && StringUtils.hasText(config.getBarkServer()) && StringUtils.hasText(config.getBarkKey())) {
                String title = "贴吧签到结果";
                String content = "成功：" + success.size() + " 失败：" + failed.size();
                HttpUtil.sendBarkNotification(config.getBarkServer(), config.getBarkKey(), title, content);
            }
            
            // 更新上次签到时间
            config.setLastSignTime(LocalDateTime.now());
            userConfigService.updateConfig(config);
            
            return signLogRepository.save(signLog);
        } catch (Exception e) {
            log.error("签到出错", e);
            signLog.setStatus("失败");
            signLog.setDetail("签到过程发生错误：" + e.getMessage());
            return signLogRepository.save(signLog);
        }
    }
} 