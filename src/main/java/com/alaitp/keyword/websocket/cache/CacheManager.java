package com.alaitp.keyword.websocket.cache;

import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import com.alaitp.keyword.websocket.message.ChartOptionSession;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public interface CacheManager {
    /**
     * use Guava's BiMap for two ways finding
     */
    BiMap<String, String> requestIdToUserMap = Maps.synchronizedBiMap(HashBiMap.create());

    Map<String, ChartOptionSession> requestSessionMap = new HashMap<>();

    ConcurrentMap<String, List<JobKeywordDto>> requestIdJobCacheMap = new ConcurrentHashMap<>();

}
