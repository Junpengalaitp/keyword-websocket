package com.alaitp.keyword.websocket.controller;

import com.alaitp.keyword.websocket.cache.CacheManager;
import com.alaitp.keyword.websocket.constant.Constant;
import com.alaitp.keyword.websocket.dto.ChartOptionDto;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
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
    public String onConnect(String requestId, Principal principal) {
        log.info("Received request id: {}, principal: {}", requestId, principal.getName());
        CacheManager.requestIdToUserMap.put(requestId, principal.getName());
        JSONObject res = new JSONObject();
        res.put("msgType", "res");
        return res.toJSONString();
    }

    public void sendJobKeyword(JobKeywordDto jobKeywordDto, String requestId) {
        JSONObject jobKeywordJson = JSON.parseObject(JSON.toJSONString(jobKeywordDto));
        jobKeywordJson.put("msgType", "jobKeyword");
        String user = CacheManager.requestIdToUserMap.get(requestId);
        messagingTemplate.convertAndSendToUser(user, Constant.p2pDestinationPrefix + Constant.keywordDestination, jobKeywordJson);
    }

    public void sendChartOptions(List<ChartOptionDto> chartOptionDtoList, String requestId) {
        String user = CacheManager.requestIdToUserMap.get(requestId);
        messagingTemplate.convertAndSendToUser(user, Constant.p2pDestinationPrefix + Constant.keywordDestination, chartOptionDtoList);
    }

    public void sendSessionEndMsg(String requestId) {
        String user = CacheManager.requestIdToUserMap.get(requestId);
        messagingTemplate.convertAndSendToUser(user, Constant.p2pDestinationPrefix + Constant.keywordDestination, "session end");
    }
}
