package com.alaitp.keyword.websocket.message;

import com.alaitp.keyword.websocket.cache.KeywordCache;
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

import java.util.List;

@Slf4j
@Component
public class MsgReceiver {
    @Value("${pub-sub.destination.prefix}")
    private String pubSubDestinationPrefix;
    @Value("${keyword.destination}")
    private String keywordDestination;
    private final SimpMessagingTemplate messagingTemplate;
    private final KeywordCache keywordCache;
    private final MsgService msgService;
    private final int SEND_INTERVAL = 1000; // time interval of send chart option message, avoiding front end rendering too often
    Long lastSentTime = null;

    public MsgReceiver(SimpMessagingTemplate messagingTemplate, KeywordCache keywordCache, MsgService msgService) {
        this.messagingTemplate = messagingTemplate;
        this.keywordCache = keywordCache;
        this.msgService = msgService;
    }

    @RabbitListener(queues = "${keyword.queue}")
    public void onMessage(String msg) {
        JSONObject keywordJson = JSON.parseObject(msg);
        if (Boolean.TRUE.equals(keywordJson.getBoolean("request_end"))) {
            log.info("all job processed, current request end, request id: " + keywordJson.getString("request_id"));
        }
        JobKeywordDto jobKeywordDto = JSON.parseObject(msg, JobKeywordDto.class);
        sendJobKeyword(jobKeywordDto);
        keywordCache.addKeyword(jobKeywordDto);

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
