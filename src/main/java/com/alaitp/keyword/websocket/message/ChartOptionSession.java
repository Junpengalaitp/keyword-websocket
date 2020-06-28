package com.alaitp.keyword.websocket.message;

import com.alaitp.keyword.websocket.ApplicationContextProvider;
import com.alaitp.keyword.websocket.cache.KeywordCache;
import com.alaitp.keyword.websocket.constant.Constant;
import com.alaitp.keyword.websocket.dto.ChartOptionDto;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import com.alaitp.keyword.websocket.service.MsgService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * each request id have it's own chart option sending session
 */
@Slf4j
@Data
public class ChartOptionSession {
    private MsgService msgService = ApplicationContextProvider.getBean(MsgService.class);
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
    private final int totalJobs;
    private final int intervalPerJob;
    private int maxJobCountPerInterval;
    /**
     * lastSendTime: the last time when sent chart options.
     * jobOptionAmount: the amount of job processed.
     */
    private long lastSendTime = 0L;
    private int jobOptionAmount = 0;

    public ChartOptionSession(int totalJobs) {
        this.totalJobs = totalJobs;
        this.maxJobCountPerInterval = totalJobs / MIN_INTERVALS;
        this.intervalPerJob = SEND_INTERVAL * MIN_INTERVALS / totalJobs;
    }

    /**
     * add jobKeywordDto to pending list waiting for scheduled sending
     * @param jobKeywordDto
     */
    public void addJobKeyword(JobKeywordDto jobKeywordDto) {
        keywordCache.addPendingKeyword(jobKeywordDto);
    }

    public void sendOnInit() {
        if (keywordCache.pendingEmpty()) {
            return;
        }
        for (int i = 0; i < keywordCache.pendingSize(); i++) {
            keywordCache.processPendingJobKeyword();
            jobOptionAmount++;
        }
        this.send();
        log.info("========> chart options sent on init, size: " + keywordCache.pendingSize());
    }

    /**
     * check every sending interval, if there is pending job keywords, process them and send
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
        log.info("========> chart options sent on interval, size: " + processSize);
    }

    private synchronized List<ChartOptionDto> getTop10ChartOptions() {
        List<ChartOptionDto> chartOptionDtoList = new ArrayList<>();
        for (String category: Constant.availableCategories) {
            ChartOptionDto chartOptionDto = keywordCache.getTopKeywordByCategory(category, 10);
            chartOptionDtoList.add(chartOptionDto);
        }
        return chartOptionDtoList;
    }

    public synchronized void send() {
        List<ChartOptionDto> chartOptions = getTop10ChartOptions();
        msgService.sendChartOptions(chartOptions);
        log.info("chart option sent, total jobs: {}, job processed: {}", totalJobs, jobOptionAmount);
        lastSendTime = System.currentTimeMillis();
    }

    public boolean sessionEnd() {
        return jobOptionAmount >= this.totalJobs - 1;
    }
}
