package com.alaitp.keyword.websocket.cache;

import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import com.alaitp.keyword.websocket.message.ChartOptionSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public interface CacheManager {
    ConcurrentMap<String, String> requestIdToUserMap = new ConcurrentHashMap<>();

    ConcurrentMap<String, List<JobKeywordDto>> requestIdJobCacheMap = new ConcurrentHashMap<>();

    Map<String, ChartOptionSession> requestSessionMap = new HashMap<>();
}
