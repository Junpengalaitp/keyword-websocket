package com.alaitp.keyword.websocket.message;

import com.alaitp.keyword.websocket.cache.KeywordCache;
import com.alaitp.keyword.websocket.constant.ConfigValue;
import com.alaitp.keyword.websocket.controller.WsController;
import com.alaitp.keyword.websocket.dto.ChartOptionDto;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import com.alaitp.keyword.websocket.util.ApplicationContextProvider;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

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
     * MIN_CHART_OPTION_RENDER_SECOND: the minimum seconds for sending total chart options for all jobs per request,
     * in case of the job keywords coming too fast (cached), keep the front end chart race visual effect steady.
     * <p>
     * SEND_INTERVAL: time interval(milliseconds) of sending chart option message, avoiding front end rendering too often
     * <p>
     * MIN_INTERVALS: minimum total intervals to send chart options per request.
     */
    public static final int SEND_INTERVAL = 500;
    private static final int MIN_CHART_OPTION_RENDER_SECOND = 20;
    private static final int MIN_INTERVALS = MIN_CHART_OPTION_RENDER_SECOND * 1000 / SEND_INTERVAL;
    private final LongAdder jobOptionAmount = new LongAdder();
    private int intervalPerJob = 0;
    private int maxJobCountPerInterval;
    /**
     * totalJobs: total amount of jobs for this request
     * <p>
     * intervalPerJob: the time interval(milliseconds) between each jobs, if the next job
     * received time is out side this interval, send it directly.
     * <p>
     * maxJobCountPerInterval: max amount of jobs to process for chart options per sending interval, at least 1
     */
    private int totalJobs = 0;
    /**
     * lastSendTime: the last time when sent chart options.
     * <p>
     * jobOptionAmount: the amount of job processed for this request id.
     */
    private long lastSendTime = 0L;

    public ChartOptionSession(int totalJobs, String requestId) {
        this.requestId = requestId;
        this.totalJobs = totalJobs;
        this.maxJobCountPerInterval = Math.max(totalJobs / MIN_INTERVALS, 1);
        this.intervalPerJob = SEND_INTERVAL * MIN_INTERVALS / totalJobs;
        log.info("ChartOptionSession for request id: {} created, total jobs: {}, max job count per interval: {}, interval per job: {}",
                this.requestId, this.totalJobs, this.maxJobCountPerInterval, this.intervalPerJob);
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
            jobOptionAmount.increment();
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
        return jobOptionAmount.intValue() == this.totalJobs;
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
