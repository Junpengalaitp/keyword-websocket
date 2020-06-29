package com.alaitp.keyword.websocket.controller;

import com.alaitp.keyword.websocket.cache.CacheManager;
import com.alaitp.keyword.websocket.constant.Constant;
import com.alaitp.keyword.websocket.dto.ChartOptionDto;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import com.alaitp.keyword.websocket.message.ChartOptionSession;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
public class WsController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/keyword")
    @SendToUser("/topic/keyword")
    public String onConnect(String msg, Principal principal) {
        log.info("Received onConnect message {}", msg);
        CacheManager.chartOptionSessionCache.putIfAbsent(principal.getName(), new ChartOptionSession());
        log.info("principal: " + principal.getName() + " added to chartOptionSessionCache");
        return "server received on onConnect message";
    }

    public void sendJobKeyword(JobKeywordDto jobKeywordDto) {
        JSONObject jobKeywordJson = JSON.parseObject(JSON.toJSONString(jobKeywordDto));
        jobKeywordJson.put("msgType", "jobKeyword");
        messagingTemplate.convertAndSend(Constant.pubSubDestinationPrefix + Constant.keywordDestination, jobKeywordJson);
    }

    public void sendChartOptions(List<ChartOptionDto> chartOptionDtoList) {
        messagingTemplate.convertAndSend(Constant.pubSubDestinationPrefix + Constant.keywordDestination, chartOptionDtoList);
    }
}
