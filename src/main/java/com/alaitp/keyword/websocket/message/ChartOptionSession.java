package com.alaitp.keyword.websocket.message;

import com.alaitp.keyword.websocket.ApplicationContextProvider;
import com.alaitp.keyword.websocket.cache.KeywordCache;
import com.alaitp.keyword.websocket.constant.ConfigValue;
import com.alaitp.keyword.websocket.controller.WsController;
import com.alaitp.keyword.websocket.dto.ChartOptionDto;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static com.alaitp.keyword.websocket.cache.CacheManager.*;

/**
 * Each request id has its own chart option sending session
 *
 * @see KeywordCache
 */
@Slf4j
@Data
@NoArgsConstructor
public class ChartOptionSession {
    private WsController wsController = ApplicationContextProvider.getBean(WsController.class);
    private String requestId;
    /**
     * cache job keywords for generating chart options for top 10.
     */
    private KeywordCache keywordCache = new KeywordCache();
    /**
     * MIN_CHART_OPTION_RENDER_SECOND: the minimum seconds for sending total chart options for all jobs per request, in
     *      case the job keywords coming too fast (cached), keep the front end chart race visual effect.
     * SEND_INTERVAL: time interval(milliseconds) of sending chart option message, avoiding front end rendering too often
     * MIN_INTERVALS: minimum total intervals to send chart options per request.
     */
    private static final int MIN_CHART_OPTION_RENDER_SECOND = 10;
    public static final int SEND_INTERVAL = 250;
    private static final int MIN_INTERVALS = MIN_CHART_OPTION_RENDER_SECOND * 1000 / SEND_INTERVAL;
    /**
     * totalJobs: total amount of jobs for this request
     * intervalPerJob: the time interval(milliseconds) between each jobs, if the next job received time is out side this interval, send it directly
     * maxJobCountPerInterval: max amount of jobs to process for chart options per sending interval
     */
    private int totalJobs = 0;
    private int intervalPerJob = 0;
    private int maxJobCountPerInterval;
    /**
     * lastSendTime: the last time when sent chart options.
     * jobOptionAmount: the amount of job processed.
     */
    private long lastSendTime = 0L;
    private int jobOptionAmount = 0;

    public ChartOptionSession(int totalJobs, String requestId) {
        this.requestId = requestId;
        this.totalJobs = totalJobs;
        this.maxJobCountPerInterval = totalJobs / MIN_INTERVALS;
        this.intervalPerJob = SEND_INTERVAL * MIN_INTERVALS / totalJobs;
    }

    /**
     * add jobKeywordDto to pending list waiting for scheduled sending.
     */
    public void addJobKeyword(JobKeywordDto jobKeywordDto) {
        keywordCache.addPendingKeyword(jobKeywordDto);
    }

    /**
     * check every sending interval, if there are pending job keywords, process them and send.
     */
    public void sendOnInterval() {
        if (keywordCache.pendingEmpty()) {
            return;
        }
        int pendingSize = keywordCache.pendingSize();
        int processSize = Math.min(maxJobCountPerInterval, pendingSize);
        for (int i = 0; i < processSize; i++) {
            keywordCache.processPendingJobKeyword();
            jobOptionAmount++;
        }
        this.send();
        log.debug("========> chart options sent on interval, size: " + processSize);
    }

    private List<ChartOptionDto> getTop10ChartOptions() {
        List<ChartOptionDto> chartOptionDtoList = new ArrayList<>();
        for (String category : ConfigValue.availableCategories) {
            ChartOptionDto chartOptionDto = keywordCache.getTopKeywordByCategory(category, 10);
            chartOptionDtoList.add(chartOptionDto);
        }
        return chartOptionDtoList;
    }

    public void send() {
        List<ChartOptionDto> chartOptions = getTop10ChartOptions();
        wsController.sendChartOptions(chartOptions, requestId);
        log.debug("chart option sent, total jobs: {}, job processed: {}", totalJobs, jobOptionAmount);
        lastSendTime = System.currentTimeMillis();
    }

    public boolean isSessionEnd() {
        return jobOptionAmount >= this.totalJobs - 1;
    }

    public void endSession() {
        wsController.sendSessionEndMsg(requestId);
        requestSessionMap.remove(requestId);
        requestIdToUserMap.remove(requestId);
        requestIdJobCacheMap.remove(requestId);
        log.info("all job processed, current request end, request id: " + requestId);
    }

    /**
     * check every session did finish the job before GC.
     */
    @Override
    protected void finalize() throws Throwable {
        if (!keywordCache.pendingEmpty()) {
            log.error("chart option session for request id: {} has pending keyword cache left", requestId);
        }
        super.finalize();
    }
}
