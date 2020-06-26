package com.alaitp.keyword.websocket.message;

import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import com.alaitp.keyword.websocket.thread.ScheduleSendThread;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class MsgReceiver {
    @Value("${pub-sub.destination.prefix}")
    private String pubSubDestinationPrefix;
    @Value("${keyword.destination}")
    private String keywordDestination;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    private String currentRequestId = null;
    private final Map<String, ChartOptionSession> requestSessionMap = new HashMap<>();


    @RabbitListener(queues = "${keyword.queue}")
    public void onMessage(String msg) {
//        log.info("receive job: " + msg);
        JSONObject keywordJson = JSON.parseObject(msg);
        String requestId =  keywordJson.getString("request_id");
        int jobNum = keywordJson.getInteger("job_number");
        int totalJobs = keywordJson.getInteger("total_job_count");

        requestSessionMap.putIfAbsent(requestId, new ChartOptionSession(totalJobs));
        ChartOptionSession chartOptionSession = requestSessionMap.get(requestId);

        if (Boolean.TRUE.equals(keywordJson.getBoolean("request_end"))) {
            log.info("all job processed, current request end, request id: " + requestId);
            requestSessionMap.remove(requestId);
            return;
        }

        JobKeywordDto jobKeywordDto = JSON.parseObject(msg, JobKeywordDto.class);
        // send job keywords
        sendJobKeyword(jobKeywordDto);

        // send chart options on receive (need check)
        chartOptionSession.sendOnReceive(jobKeywordDto);

        // send chart options by time interval
        if (!requestId.equals(currentRequestId)) {
            log.info("started timed sending thread for request id: " + requestId);
            ScheduleSendThread scheduleSendThread = new ScheduleSendThread(chartOptionSession);
            scheduleSendThread.start();
        }
        this.currentRequestId = requestId;
    }

    private void sendJobKeyword(JobKeywordDto jobKeywordDto) {
        JSONObject jobKeywordJson = JSON.parseObject(JSON.toJSONString(jobKeywordDto));
        jobKeywordJson.put("msgType", "jobKeyword");
        messagingTemplate.convertAndSend(pubSubDestinationPrefix + keywordDestination, jobKeywordJson);
    }
}
