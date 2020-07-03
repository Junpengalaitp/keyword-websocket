package com.alaitp.keyword.websocket.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;

@Component
public class Constant {
    @Value("${pub-sub.destination.prefix}")
    private String pubSubDestinationPrefixValue;
    @Value("${keyword.destination}")
    private String keywordDestinationValue;
    @Value("${available.keyword.category}")
    private String categoriesValue;
    public static String p2pDestinationPrefix;

    public static String pubSubDestinationPrefix;
    public static String keywordDestination;
    public static String categories;
    @Value("${point-point.destination.prefix}")
    private String p2pDestinationPrefixValue;
    public static Set<String> availableCategories;

    @PostConstruct
    private void init() {
        pubSubDestinationPrefix = pubSubDestinationPrefixValue;
        keywordDestination = keywordDestinationValue;
        categories = categoriesValue;
        p2pDestinationPrefix = p2pDestinationPrefixValue;
        availableCategories = new HashSet<>();
        availableCategories.addAll(Arrays.asList(Constant.categories.split(",")));
    }

    public static final Map<String, String> combinedCategoryMap = Map.ofEntries(
            entry("PROGRAMMING_LANGUAGE", "languages"),
            entry("OTHER_LANGUAGE", "languages"),
            entry("LIBRARY", "libraryFramework"),
            entry("FRAMEWORK", "libraryFramework"),
            entry("DATA_STORAGE", "data"),
            entry("DATA_TRANSMISSION", "data"),
            entry("DIVISION", "division"),
            entry("POSITION", "division"),
            entry("PLATFORM", "platform"),
            entry("APPROACH", "approach"),
            entry("SOFTWARE_ENGINEERING", "swe"),
            entry("GENERAL", "general"),
            entry("SOFT_SKILL", "general"),
            entry("PROTOCOL", "protocol"),
            entry("COMPUTER_SCIENCE", "computerScience"),
            entry("AI", "computerScience")
    );

    public static final int ONE_SEC = 1000;
}


