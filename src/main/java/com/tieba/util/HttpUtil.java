package com.tieba.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

/**
 * HTTP请求工具类
 */
@Slf4j
public class HttpUtil {

    /**
     * 发送GET请求
     *
     * @param url     请求URL
     * @param headers 请求头
     * @return 响应JSON
     */
    public static JSONObject doGet(String url, Map<String, String> headers) {
        try {
            HttpClient client = HttpClients.createDefault();
            HttpGet request = new HttpGet(url);
            
            // 设置请求头
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    request.addHeader(entry.getKey(), entry.getValue());
                }
            }
            
            HttpResponse response = client.execute(request);
            String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            return JSON.parseObject(result);
        } catch (Exception e) {
            log.error("GET请求出错 - URL: {}, 错误: {}", url, e.getMessage());
            return new JSONObject();
        }
    }

    /**
     * 发送GET请求
     *
     * @param url 请求URL
     * @return 响应JSON
     */
    public static JSONObject doGet(String url) {
        return doGet(url, Collections.emptyMap());
    }

    /**
     * 发送POST请求
     *
     * @param url     请求URL
     * @param body    请求体
     * @param headers 请求头
     * @return 响应JSON
     */
    public static JSONObject doPost(String url, String body, Map<String, String> headers) {
        try {
            HttpClient client = HttpClients.createDefault();
            HttpPost request = new HttpPost(url);
            
            // 设置请求头
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    request.addHeader(entry.getKey(), entry.getValue());
                }
            }
            
            // 设置请求体
            if (StringUtils.hasText(body)) {
                request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
            }
            
            HttpResponse response = client.execute(request);
            String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            return JSON.parseObject(result);
        } catch (IOException e) {
            log.error("POST请求出错 - URL: {}, 错误: {}", url, e.getMessage());
            return new JSONObject();
        }
    }

    /**
     * 发送Bark推送
     *
     * @param server  Bark服务器
     * @param key     Bark推送Key
     * @param title   推送标题
     * @param content 推送内容
     * @return 是否成功
     */
    public static boolean sendBarkNotification(String server, String key, String title, String content) {
        if (!StringUtils.hasText(server) || !StringUtils.hasText(key)) {
            log.warn("Bark推送参数缺失");
            return false;
        }
        
        try {
            String url = server.endsWith("/") ? server : server + "/";
            url = url + key + "/" + title + "/" + content;
            
            HttpClient client = HttpClients.createDefault();
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            
            return response.getStatusLine().getStatusCode() == 200;
        } catch (Exception e) {
            log.error("Bark推送失败 - {}", e.getMessage());
            return false;
        }
    }
} 