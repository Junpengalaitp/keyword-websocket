package com.alaitp.keyword.websocket.controller;

import com.alaitp.keyword.websocket.cache.CacheManager;
import com.alaitp.keyword.websocket.constant.ConfigValue;
import com.alaitp.keyword.websocket.dto.ChartOptionDto;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import com.alaitp.keyword.websocket.thread.ScheduleThreadPool;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static com.alaitp.keyword.websocket.constant.Constant.*;

@Slf4j
@Controller
public class WsController {
    private final ConfigValue configValue;
    private final SimpMessagingTemplate messagingTemplate;

    public WsController(ConfigValue configValue, SimpMessagingTemplate messagingTemplate) {
        this.configValue = configValue;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * record the session(principal user) on ws connect, map the request id to users
     */
    @MessageMapping("${value.ws.destination.keyword}")
    public void onConnect(String requestId, Principal principal) {
        log.info("Received request id: {}, principal: {}", requestId, principal.getName());
        if (CacheManager.REQUEST_ID_TO_USER_MAP.containsValue(principal.getName())) {
            log.info("user send request before previous request complete, cancel scheduled thread");
            ScheduleThreadPool.endTask(CacheManager.REQUEST_ID_TO_USER_MAP.inverse().get(principal.getName()));
            CacheManager.REQUEST_ID_TO_USER_MAP.inverse().remove(principal.getName());
        }
        CacheManager.REQUEST_ID_TO_USER_MAP.put(requestId, principal.getName());
    }

    /**
     * send the job keyword to user for this request id. Due to async issue (job keyword arrive before the request id
     * recorded, use a cache to cache those pre-arrived jobKeywordDto and send them later)
     */
    public void sendJobKeyword(JobKeywordDto jobKeywordDto, String requestId) {
        JSONObject jobKeywordJson = JSON.parseObject(JSON.toJSONString(jobKeywordDto));
        jobKeywordJson.put(MSG_TYPE, TYPE_JOB_KEYWORD);
        String user = CacheManager.REQUEST_ID_TO_USER_MAP.get(requestId);
        if (user == null) {
            CacheManager.REQUEST_ID_JOB_CACHE_MAP.computeIfAbsent(requestId, k -> new ArrayList<>());
            CacheManager.REQUEST_ID_JOB_CACHE_MAP.get(requestId).add(jobKeywordDto);
        } else {
            messagingTemplate.convertAndSendToUser(user, configValue.keywordSendingDestination(), jobKeywordJson);
            sendCachedJobKeywords(requestId);
        }
    }

    /**
     * send cached keywords
     */
    private void sendCachedJobKeywords(String requestId) {
        List<JobKeywordDto> cacheJobKeyword = CacheManager.REQUEST_ID_JOB_CACHE_MAP.get(requestId);
        if (cacheJobKeyword != null && !cacheJobKeyword.isEmpty()) {
            String user = CacheManager.REQUEST_ID_TO_USER_MAP.get(requestId);
            for (JobKeywordDto jobKeywordCache : cacheJobKeyword) {
                JSONObject jobKeywordJson = JSON.parseObject(JSON.toJSONString(jobKeywordCache));
                jobKeywordJson.put(MSG_TYPE, TYPE_JOB_KEYWORD);
                messagingTemplate.convertAndSendToUser(user, configValue.keywordSendingDestination(), jobKeywordJson);
            }
            log.info(cacheJobKeyword.size() + " cached job keyword sent");
            cacheJobKeyword.clear();
        }
    }

    public void sendChartOptions(List<ChartOptionDto> chartOptionDtoList, String requestId) {
        String user = CacheManager.REQUEST_ID_TO_USER_MAP.get(requestId);
        messagingTemplate.convertAndSendToUser(user, configValue.chartSendingDestination(), chartOptionDtoList);
    }

    public void sendSessionEndMsg(String requestId) {
        String user = CacheManager.REQUEST_ID_TO_USER_MAP.get(requestId);
        messagingTemplate.convertAndSendToUser(user, configValue.chartSendingDestination(), TYPE_SESSION_END);
    }
}
