package com.alaitp.keyword.websocket.cache;

import com.alaitp.keyword.websocket.dto.JobKeywordDto;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public interface CacheManager {
    ConcurrentMap<String, String> requestIdToUserMap = new ConcurrentHashMap<>();

    ConcurrentMap<String, List<JobKeywordDto>> requestIdJobCacheMap = new ConcurrentHashMap<>();
}
