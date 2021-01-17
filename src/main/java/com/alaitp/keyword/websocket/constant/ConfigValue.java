package com.alaitp.keyword.websocket.constant;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Getter
@Component
public class ConfigValue {
    @Value("${value.ws.destination.pub-sub-prefix}")
    private String pubSubDestinationPrefix;
    @Value("${value.ws.destination.point-point-prefix}")
    private String p2pDestinationPrefix;
    @Value("${value.ws.destination.keyword}")
    private String keywordDestination;
    @Value(("${value.ws.destination.chart}"))
    private String chartDestination;
    @Value("${value.available-keyword-category}")
    private String categories;
    @Value("${alaitp.frontend.uri}")
    private String frontendUri;
    @Value("${value.ws.destination.endpoint}")
    private String wsEndpoint;
    @Value("${value.ws.destination.app-prefix}")
    private String appDestinationPrefix;

    @PostConstruct
    private void loggingConfigValues() {
        log.info("pubSubDestinationPrefix config value: {}", pubSubDestinationPrefix);
        log.info("p2pDestinationPrefix config value: {}", p2pDestinationPrefix);
        log.info("keywordDestination config value: {}", keywordDestination);
        log.info("chartDestination config value: {}", chartDestination);
        log.info("frontendUri config value: {}", frontendUri);
        log.info("wsEndpoint config value: {}", wsEndpoint);
        log.info("appDestinationPrefix config value: {}", appDestinationPrefix);
        log.info("categories config value: {}", categories);
    }

    public String keywordSendingDestination() {
        return p2pDestinationPrefix + keywordDestination;
    }

    public String chartSendingDestination() {
        return p2pDestinationPrefix + chartDestination;
    }
}


