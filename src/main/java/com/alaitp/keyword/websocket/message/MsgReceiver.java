package com.alaitp.keyword.websocket.message;

import com.alaitp.keyword.websocket.cache.CacheManager;
import com.alaitp.keyword.websocket.controller.WsController;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import com.alaitp.keyword.websocket.thread.ScheduleThreadPool;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.alaitp.keyword.websocket.constant.Constant.REQUEST_ID;
import static com.alaitp.keyword.websocket.constant.Constant.TOTAL_JOB_COUNT;

/**
 * on receive a job keyword message, do these two things:
 * 1. send the job keyword to front end immediately.
 * 2. collect job keywords for chart options, use a scheduled thread to send top K chart options to front end.
 */
@Slf4j
@Component
public class MsgReceiver {
    @Autowired
    private WsController wsController;
    private String currentRequestId = null;

    @RabbitListener(queues = "${keyword.queue}")
    public void onMessage(String msg) {
        JobKeywordDto jobKeywordDto = JSON.parseObject(msg, JobKeywordDto.class);
        JSONObject keywordJson = JSON.parseObject(msg);
        String requestId = keywordJson.getString(REQUEST_ID);

        // send job keywords on receive (no check needed)
        wsController.sendJobKeyword(jobKeywordDto, requestId);

        ChartOptionSession chartOptionSession = getChartOptionSession(keywordJson);
        // add jobKeywordDto to pending list for chart options
        chartOptionSession.addJobKeyword(jobKeywordDto);
        // send chart options by time interval
        if (isRequestStart(requestId)) {
            ScheduleThreadPool.submit(chartOptionSession);
        }
        currentRequestId = requestId;
    }

    private ChartOptionSession getChartOptionSession(JSONObject keywordJson) {
        String requestId = keywordJson.getString(REQUEST_ID);
        int totalJobs = keywordJson.getInteger(TOTAL_JOB_COUNT);
        CacheManager.REQUEST_SESSION_MAP.computeIfAbsent(requestId, k -> new ChartOptionSession(totalJobs, requestId));
        return CacheManager.REQUEST_SESSION_MAP.get(requestId);
    }

    private boolean isRequestStart(String requestId) {
        return !requestId.equals(currentRequestId);
    }
}
