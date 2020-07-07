package com.alaitp.keyword.websocket.controller;

import com.alaitp.keyword.websocket.cache.CacheManager;
import com.alaitp.keyword.websocket.constant.ConfigValue;
import com.alaitp.keyword.websocket.dto.ChartOptionDto;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import com.alaitp.keyword.websocket.thread.ScheduleThreadPool;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    public static final String chartDestination = ConfigValue.p2pDestinationPrefix + ConfigValue.chartDestination;
    private static final String keywordDestination = ConfigValue.p2pDestinationPrefix + ConfigValue.keywordDestination;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * record the session(principal user) on ws connect, map the request id to users
     */
    @MessageMapping("${keyword.destination}")
    public void onConnect(String requestId, Principal principal) {
        log.info("Received request id: {}, principal: {}", requestId, principal.getName());
        if (CacheManager.requestIdToUserMap.containsValue(principal.getName())) {
            log.info("user send request before previous request complete, cancel scheduled thread");
            ScheduleThreadPool.endTask(CacheManager.requestIdToUserMap.inverse().get(principal.getName()));
            CacheManager.requestIdToUserMap.inverse().remove(principal.getName());
        }
        CacheManager.requestIdToUserMap.put(requestId, principal.getName());
    }

    /**
     * send the job keyword to user for this request id. Due to async issue (job keyword arrive before the request id
     * recorded, use a cache to cache those pre-arrived jobKeywordDto and send them later)
     */
    public void sendJobKeyword(JobKeywordDto jobKeywordDto, String requestId) {
        JSONObject jobKeywordJson = JSON.parseObject(JSON.toJSONString(jobKeywordDto));
        jobKeywordJson.put(MSG_TYPE, TYPE_JOB_KEYWORD);
        String user = CacheManager.requestIdToUserMap.get(requestId);
        if (user == null) {
            CacheManager.requestIdJobCacheMap.putIfAbsent(requestId, new ArrayList<>());
            CacheManager.requestIdJobCacheMap.get(requestId).add(jobKeywordDto);
        } else {
            messagingTemplate.convertAndSendToUser(user, keywordDestination, jobKeywordJson);
            sendCachedJobKeywords(requestId);
        }
    }

    /**
     * send cached keywords
     */
    private void sendCachedJobKeywords(String requestId) {
        List<JobKeywordDto> cacheJobKeyword = CacheManager.requestIdJobCacheMap.get(requestId);
        if (cacheJobKeyword != null && !cacheJobKeyword.isEmpty()) {
            String user = CacheManager.requestIdToUserMap.get(requestId);
            for (JobKeywordDto jobKeywordCache : cacheJobKeyword) {
                JSONObject jobKeywordJson = JSON.parseObject(JSON.toJSONString(jobKeywordCache));
                jobKeywordJson.put(MSG_TYPE, TYPE_JOB_KEYWORD);
                messagingTemplate.convertAndSendToUser(user, keywordDestination, jobKeywordJson);
            }
            log.info(cacheJobKeyword.size() + " cached job keyword sent");
            cacheJobKeyword.clear();
        }
    }

    public void sendChartOptions(List<ChartOptionDto> chartOptionDtoList, String requestId) {
        String user = CacheManager.requestIdToUserMap.get(requestId);
        messagingTemplate.convertAndSendToUser(user, chartDestination, chartOptionDtoList);
    }

    public void sendSessionEndMsg(String requestId) {
        String user = CacheManager.requestIdToUserMap.get(requestId);
        messagingTemplate.convertAndSendToUser(user, chartDestination, TYPE_SESSION_END);
    }
}
