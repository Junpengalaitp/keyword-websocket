package com.alaitp.keyword.websocket.message;

import com.alaitp.keyword.websocket.ApplicationContextProvider;
import com.alaitp.keyword.websocket.cache.KeywordCache;
import com.alaitp.keyword.websocket.dto.ChartOptionDto;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;

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

    private Set<String> availableCategories;
    private SimpMessagingTemplate messagingTemplate = ApplicationContextProvider.getBean(SimpMessagingTemplate.class);
    private Long lastSendTime = null;
    private final int SEND_INTERVAL = 1000; // time interval of send chart option message, avoiding front end rendering too often
    private final int MIN_INTERVALS = 5;  // the minimal seconds for sending chart options, for keeping chart race visual effect
    private int maxJobCountPerInterval;  // the max job chart option processed each sending interval
    private boolean started = false;

    public ChartOptionSession(int totalJobs) {
        this.availableCategories = new HashSet<>();
        this.availableCategories.addAll(Arrays.asList(categories.split(",")));
        this.maxJobCountPerInterval = totalJobs / MIN_INTERVALS;
    }

    private KeywordCache keywordCache = new KeywordCache();

    public void cacheKeyword(JobKeywordDto jobKeywordDto) {
        keywordCache.addKeyword(jobKeywordDto);
        checkSend();
    }

    public void checkSend() {
        if (lastSendTime != null && System.currentTimeMillis() - lastSendTime < SEND_INTERVAL) {
            return;
        }
        if (keywordCache.size() >= maxJobCountPerInterval) {
            for (int i = 0; i < maxJobCountPerInterval; i++) {
                keywordCache.processPendingJobKeyword();
            }
            List<ChartOptionDto> chartOptions = getChartOptions();
            messagingTemplate.convertAndSend(pubSubDestinationPrefix + keywordDestination, chartOptions);
            log.info("chart option sent: " + chartOptions);
            lastSendTime = System.currentTimeMillis();
        }
    }

    private List<ChartOptionDto> getChartOptions() {
        List<ChartOptionDto> chartOptionDtos = new ArrayList<>();
        for (String category: availableCategories) {
            ChartOptionDto chartOptionDto = keywordCache.getTopKeywordByCategory(category, 10);
            chartOptionDtos.add(chartOptionDto);
        }
        return chartOptionDtos;
    }
}
