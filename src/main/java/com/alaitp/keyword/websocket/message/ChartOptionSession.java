package com.alaitp.keyword.websocket.message;

import com.alaitp.keyword.websocket.cache.KeywordCache;
import com.alaitp.keyword.websocket.dto.ChartOptionDto;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import com.alaitp.keyword.websocket.service.MsgService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

import static com.alaitp.keyword.websocket.constant.Constant.ONE_SEC;

/**
 * each request id have it's own chart option sending session
 */
@Slf4j
@Data
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ChartOptionSession {
    @Value("${pub-sub.destination.prefix}")
    private String pubSubDestinationPrefix;
    @Value("${keyword.destination}")
    private String keywordDestination;
    @Value("${available.keyword.category}")
    private String categories;
    private Set<String> availableCategories;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private MsgService msgService;
    private final int MIN_SECOND = 5;  // the minimal seconds for sending chart options, for keeping chart race visual effect
    private final int SEND_INTERVAL = 1000; // time interval of send chart option message, avoiding front end rendering too often
    private int maxJobCountPerInterval;  // the max job chart option processed each sending interval
    @PostConstruct
    void init() {
        availableCategories = new HashSet<>();
        availableCategories.addAll(Arrays.asList(categories.split(",")));
    }


    public ChartOptionSession(int totalJobs) {
        this.maxJobCountPerInterval = totalJobs / SEND_INTERVAL / ONE_SEC / MIN_SECOND;
    }

    private KeywordCache keywordCache = new KeywordCache(SEND_INTERVAL);

    public void cacheKeyword(JobKeywordDto jobKeywordDto) {
        keywordCache.addKeyword(jobKeywordDto);
    }

    public void sendChartOption(JobKeywordDto jobKeywordDto) {
        List<ChartOptionDto> chartOptions = msgService.getChartOptions();
        messagingTemplate.convertAndSend(pubSubDestinationPrefix + keywordDestination, chartOptions);
        log.info("send chart option: {}", chartOptions);
    }

    private List<ChartOptionDto> getChartOptions() {
        List<ChartOptionDto> chartOptionDtos = new ArrayList<>();
        for (String category: availableCategories) {
            ChartOptionDto chartOptionDto = keywordCache.getTopKeywordByCategory(category, 10);
            chartOptionDtos.add(chartOptionDto);
        }
        return chartOptionDtos;
    }

    public void timer() {
        // checking per sending interval
        // if pending jobs larger than maxJobCountPerInterval, send the maxJobCountPerInterval amount of jobs
        while (!keywordCache.isEmpty()) {

        }
    }
}
