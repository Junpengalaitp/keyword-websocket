package com.alaitp.keyword.websocket.controller;

import com.alaitp.keyword.websocket.cache.CacheManager;
import com.alaitp.keyword.websocket.constant.ConfigValue;
import com.alaitp.keyword.websocket.dto.ChartOptionDto;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

import static com.alaitp.keyword.websocket.constant.Constant.*;

@Slf4j
@Controller
public class WsController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("${keyword.destination}")
    public void onConnect(String requestId, Principal principal) {
        log.info("Received request id: {}, principal: {}", requestId, principal.getName());
        CacheManager.requestIdToUserMap.put(requestId, principal.getName());
    }

    public void sendJobKeyword(JobKeywordDto jobKeywordDto, String requestId) {
        JSONObject jobKeywordJson = JSON.parseObject(JSON.toJSONString(jobKeywordDto));
        jobKeywordJson.put(MSG_TYPE, TYPE_JOB_KEYWORD);
        String user = CacheManager.requestIdToUserMap.get(requestId);
        messagingTemplate.convertAndSendToUser(user, ConfigValue.p2pDestinationPrefix + ConfigValue.keywordDestination, jobKeywordJson);
    }

    public void sendChartOptions(List<ChartOptionDto> chartOptionDtoList, String requestId) {
        String user = CacheManager.requestIdToUserMap.get(requestId);
        messagingTemplate.convertAndSendToUser(user, ConfigValue.p2pDestinationPrefix + ConfigValue.chartDestination, chartOptionDtoList);
    }

    public void sendSessionEndMsg(String requestId) {
        String user = CacheManager.requestIdToUserMap.get(requestId);
        messagingTemplate.convertAndSendToUser(user, ConfigValue.p2pDestinationPrefix + ConfigValue.chartDestination, TYPE_SESSION_END);
    }
}
