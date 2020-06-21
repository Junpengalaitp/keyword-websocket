package com.alaitp.keyword.websocket.constant;

import java.util.Map;

import static java.util.Map.entry;

public class Constant {
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
}
