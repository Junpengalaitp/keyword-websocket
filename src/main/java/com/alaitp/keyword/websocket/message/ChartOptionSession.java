package com.alaitp.keyword.websocket.message;

import com.alaitp.keyword.websocket.ApplicationContextProvider;
import com.alaitp.keyword.websocket.cache.KeywordCache;
import com.alaitp.keyword.websocket.dto.ChartOptionDto;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * each request id have it's own chart option sending session
 */
@Slf4j
@Data
public class ChartOptionSession {
//    @Value("${pub-sub.destination.prefix}")
    private String pubSubDestinationPrefix = "/topic";
//    @Value("${keyword.destination}")
    private String keywordDestination = "/keyword";
//    @Value("${available.keyword.category}")
    private String categories = "PROGRAMMING_LANGUAGE,OTHER_LANGUAGE,LIBRARY,FRAMEWORK,DATA_STORAGE,DATA_TRANSMISSION,DIVISION,PLATFORM,APPROACH,SOFTWARE_ENGINEERING,GENERAL,SOFT_SKILL,PROTOCOL,COMPUTER_SCIENCE,AI";

    private final int totalJobs;
    private Set<String> availableCategories;
    private SimpMessagingTemplate messagingTemplate = ApplicationContextProvider.getBean(SimpMessagingTemplate.class);
    private final int TOTAL_SENDING_SECOND = 10;
    private final int SEND_INTERVAL = 250; // time interval of send chart option message, avoiding front end rendering too often
    private final int MIN_INTERVALS = TOTAL_SENDING_SECOND * 1000 / SEND_INTERVAL;  // the minimal seconds for sending chart options, for keeping chart race visual effect
    private final int INTERVAL_PER_JOB; // the time interval between each jobs, if the next job received time is out side this interval, send it directly
    private int maxJobCountPerInterval;  // the max job chart option processed each sending interval
    private boolean started = false;
    private AtomicLong lastReceiveTime = new AtomicLong(0);
    private AtomicLong lastSendTime = new AtomicLong(0);
    private AtomicInteger jobOptionAmount = new AtomicInteger(0);

    public ChartOptionSession(int totalJobs) {
        this.totalJobs = totalJobs;
        this.availableCategories = new HashSet<>();
        this.availableCategories.addAll(Arrays.asList(categories.split(",")));
        this.maxJobCountPerInterval = totalJobs / MIN_INTERVALS;
        this.INTERVAL_PER_JOB = SEND_INTERVAL * MIN_INTERVALS / totalJobs;
    }

    private KeywordCache keywordCache = new KeywordCache();

    /**
     * when checked true, send this chart option of this jobKeywordDto
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
        lastReceiveTime.set(System.currentTimeMillis());
    }

    private boolean checkSendOnReceive() {
        // when receive the first job, send it
        if (lastReceiveTime.get() == 0) {
            return true;
        } else {
            // when receive outside the interval per job (job coming too slow), send it
            return System.currentTimeMillis() - lastReceiveTime.get() >= this.INTERVAL_PER_JOB;
        }
    }

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

    private synchronized List<ChartOptionDto> getChartOptions() {
        List<ChartOptionDto> chartOptionDtos = new ArrayList<>();
        for (String category: availableCategories) {
            ChartOptionDto chartOptionDto = keywordCache.getTopKeywordByCategory(category, 10);
            chartOptionDtos.add(chartOptionDto);
        }
        return chartOptionDtos;
    }

    public synchronized void send() {
        List<ChartOptionDto> chartOptions = getChartOptions();
        messagingTemplate.convertAndSend(pubSubDestinationPrefix + keywordDestination, chartOptions);
        log.info("chart option sent, total jobs: {}, job processed: {}", totalJobs, jobOptionAmount.get());
        lastSendTime.set(System.currentTimeMillis());
    }

    public boolean sessionEnd() {
        return jobOptionAmount.get() >= this.totalJobs - 1;
    }
}
