package com.alaitp.keyword.websocket.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class ConfigValue {
    public static String p2pDestinationPrefix;
    public static String pubSubDestinationPrefix;
    public static String categories;
    public static Set<String> availableCategories;
    public static String keywordDestination;
    public static String chartDestination;
    @Value("${value.ws.destination.pub-sub-prefix}")
    private String pubSubDestinationPrefixValue;
    @Value("${value.ws.destination.point-point-prefix}")
    private String p2pDestinationPrefixValue;
    @Value("${value.ws.destination.keyword}")
    private String keywordDestinationValue;
    @Value(("${value.ws.destination.chart}"))
    private String chartDestinationValue;
    @Value("${value.available-keyword-category}")
    private String categoriesValue;

    @PostConstruct
    private void init() {
        pubSubDestinationPrefix = pubSubDestinationPrefixValue;
        categories = categoriesValue;
        p2pDestinationPrefix = p2pDestinationPrefixValue;
        availableCategories = new HashSet<>();
        availableCategories.addAll(Arrays.asList(ConfigValue.categories.split(",")));
        keywordDestination = keywordDestinationValue;
        chartDestination = chartDestinationValue;
    }
}


