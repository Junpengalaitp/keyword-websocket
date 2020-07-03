package com.alaitp.keyword.websocket.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CacheManager {
    public static final ConcurrentMap<String, String> requestIdToUserMap = new ConcurrentHashMap<>();
}
