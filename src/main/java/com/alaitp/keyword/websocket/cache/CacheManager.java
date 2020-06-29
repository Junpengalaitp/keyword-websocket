package com.alaitp.keyword.websocket.cache;

import com.alaitp.keyword.websocket.message.ChartOptionSession;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CacheManager {
    public static final ConcurrentMap<String, ChartOptionSession> chartOptionSessionCache = new ConcurrentHashMap<>();
}
