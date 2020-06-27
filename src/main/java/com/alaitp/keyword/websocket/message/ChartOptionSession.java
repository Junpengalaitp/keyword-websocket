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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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
    private final int MIN_CHART_OPTION_RENDER_SECOND = 10;
    private final int SEND_INTERVAL = 250;
    private final int MIN_INTERVALS = MIN_CHART_OPTION_RENDER_SECOND * 1000 / SEND_INTERVAL;
    /**
     * totalJobs: total amount of jobs for this request
     * intervalPerJob: the time interval(milliseconds) between each jobs, if the next job received time is out side this interval, send it directly
     * maxJobCountPerInterval: max amount of jobs to process for chart options per sending interval
     */
    private final int totalJobs;
    private final int intervalPerJob;
    private int maxJobCountPerInterval;
    /**
     * lastReceiveTime: the last time when received job keywords, for checking sendOnReceive, only one thread access it
     * lastSendTime: the last time when sent chart options, two threads access it(sendOnReceive, sendOnInterval)
     * jobOptionAmount: the amount of job processed, two threads access it(sendOnReceive, sendOnInterval)
     */
    private long lastReceiveTime = 0L;
    private AtomicLong lastSendTime = new AtomicLong(0);
    private AtomicInteger jobOptionAmount = new AtomicInteger(0);

    public ChartOptionSession(int totalJobs) {
        this.totalJobs = totalJobs;
        this.maxJobCountPerInterval = totalJobs / MIN_INTERVALS;
        this.intervalPerJob = SEND_INTERVAL * MIN_INTERVALS / totalJobs;
    }

    /**
     * when checked true (jobKeywordDto coming slow), send this chart option of this jobKeywordDto
     * else add this jobKeywordDto to pending list waiting for scheduled sending
     * @param jobKeywordDto
     */
    public void sendOnReceive(JobKeywordDto jobKeywordDto) {
        if (checkSendOnReceive()) {
            log.info("=============> sending on receive");
            keywordCache.addSendingKeyword(jobKeywordDto);
            jobOptionAmount.incrementAndGet();
            this.send();
        } else {
            keywordCache.addPendingKeyword(jobKeywordDto);
        }
        lastReceiveTime = System.currentTimeMillis();
    }

    private boolean checkSendOnReceive() {
        // when receive the first job, send it
        if (lastReceiveTime == 0) {
            return true;
        } else {
            // when receive outside the interval per job (job coming too slow), send it
            return System.currentTimeMillis() - lastReceiveTime >= this.intervalPerJob;
        }
    }

    /**
     * check every sending interval, if there is pending job keywords, process them and send
     */
    public void sendOnInterval() {
        if (lastSendTime != null && System.currentTimeMillis() - lastSendTime.get() < SEND_INTERVAL) {
            return;
        }
        if (keywordCache.pendingEmpty()) {
            return;
        }
        int pendingSize = keywordCache.pendingSize();
        for (int i = 0; i < Math.min(maxJobCountPerInterval, pendingSize); i++) {
            keywordCache.processPendingJobKeyword();
            jobOptionAmount.incrementAndGet();
        }
        this.send();
        log.info("========> chart options sent on interval, pending size: " + pendingSize);
    }

    private synchronized List<ChartOptionDto> getTop10ChartOptions() {
        List<ChartOptionDto> chartOptionDtos = new ArrayList<>();
        for (String category: Constant.availableCategories) {
            ChartOptionDto chartOptionDto = keywordCache.getTopKeywordByCategory(category, 10);
            chartOptionDtos.add(chartOptionDto);
        }
        return chartOptionDtos;
    }

    public synchronized void send() {
        List<ChartOptionDto> chartOptions = getTop10ChartOptions();
        msgService.sendChartOptions(chartOptions);
        log.info("chart option sent, total jobs: {}, job processed: {}", totalJobs, jobOptionAmount.get());
        lastSendTime.set(System.currentTimeMillis());
    }

    public boolean sessionEnd() {
        return jobOptionAmount.get() >= this.totalJobs - 1;
    }
}
