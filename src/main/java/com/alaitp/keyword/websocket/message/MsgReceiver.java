package com.alaitp.keyword.websocket.message;

import com.alaitp.keyword.websocket.dto.ChartOptionDto;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import com.alaitp.keyword.websocket.service.MsgService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MsgReceiver {
    @Value("${pub-sub.destination.prefix}")
    private String pubSubDestinationPrefix;
    @Value("${keyword.destination}")
    private String keywordDestination;
    private final SimpMessagingTemplate messagingTemplate;
    private final MsgService msgService;
    private final int MIN_SECOND = 5;  // the minimal seconds for sending chart options, for keeping chart race visual effect
    private final int SEND_INTERVAL = 1000; // time interval of send chart option message, avoiding front end rendering too often
    private Long lastSentTime = null;
    private String currentRequestId = null;
    private final Map<String, ChartOptionSession> requestSessionMap = new HashMap<>();

    public MsgReceiver(SimpMessagingTemplate messagingTemplate, MsgService msgService) {
        this.messagingTemplate = messagingTemplate;
        this.msgService = msgService;
    }

    @RabbitListener(queues = "${keyword.queue}")
    public void onMessage(String msg) {
        JSONObject keywordJson = JSON.parseObject(msg);
        String requestId =  keywordJson.getString("request_id");
        int totalJobs = keywordJson.getInteger("total_job_count");
        requestSessionMap.putIfAbsent(requestId, new ChartOptionSession(totalJobs));
        ChartOptionSession chartOptionSession = requestSessionMap.get(requestId);
        if (Boolean.TRUE.equals(keywordJson.getBoolean("request_end"))) {
            log.info("all job processed, current request end, request id: " + requestId);
            requestSessionMap.remove(requestId);
        }
        log.info("total jobs: " + totalJobs);
        JobKeywordDto jobKeywordDto = JSON.parseObject(msg, JobKeywordDto.class);

        sendJobKeyword(jobKeywordDto);

        chartOptionSession.cacheKeyword(jobKeywordDto);

        if (lastSentTime == null || System.currentTimeMillis() - lastSentTime > SEND_INTERVAL) {
            sendChartOption(jobKeywordDto);
            lastSentTime = System.currentTimeMillis();
        }
    }

    private void sendJobKeyword(JobKeywordDto jobKeywordDto) {
        JSONObject jobKeywordJson = JSON.parseObject(JSON.toJSONString(jobKeywordDto));
        jobKeywordJson.put("msgType", "jobKeyword");
        messagingTemplate.convertAndSend(pubSubDestinationPrefix + keywordDestination, jobKeywordJson);
    }

    private void sendChartOption(JobKeywordDto jobKeywordDto) {
        List<ChartOptionDto> chartOptions = msgService.getChartOptions(jobKeywordDto);
        messagingTemplate.convertAndSend(pubSubDestinationPrefix + keywordDestination, chartOptions);
        log.info("send chart option: {}", chartOptions);
    }
}
