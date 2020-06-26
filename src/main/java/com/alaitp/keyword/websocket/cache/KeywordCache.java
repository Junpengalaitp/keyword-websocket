package com.alaitp.keyword.websocket.cache;

import com.alaitp.keyword.websocket.constant.Constant;
import com.alaitp.keyword.websocket.dto.ChartOptionDto;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * store job keywords to calculate chart options data for front end
 */
@Slf4j
public class KeywordCache {
    // the map to get chart options
    private final ConcurrentMap<String, ConcurrentMap<String, Integer>> keywordChartOptionMap = new ConcurrentHashMap<>();

    // when receiving JobKeywordDto out of sending interval, add to pending list and use them in the next sending interval
    private ConcurrentLinkedQueue<JobKeywordDto> pendingList = new ConcurrentLinkedQueue<>();


    public void addPendingKeyword(JobKeywordDto jobKeywordDto) {
        pendingList.offer(jobKeywordDto);
    }

    public void addSendingKeyword(JobKeywordDto jobKeywordDto) {
        addJobKeywordToMap(jobKeywordDto);
    }

    public void processPendingJobKeyword() {
        JobKeywordDto jobKeywordDto = pendingList.remove();
        addJobKeywordToMap(jobKeywordDto);
    }

    public void addJobKeywordToMap(JobKeywordDto jobKeywordDto) {
        List<JobKeywordDto.KeywordDto> keywordDtoList = jobKeywordDto.getKeywordList();
        for (JobKeywordDto.KeywordDto keywordDto : keywordDtoList) {
            String category = keywordDto.getCategory();
            String combinedCategory = Constant.combinedCategoryMap.getOrDefault(category, category);
            String keyword = keywordDto.getKeyword();
            ConcurrentMap<String, Integer> keywordCount = keywordChartOptionMap.get(combinedCategory);
            if (keywordCount == null) {
                keywordCount = new ConcurrentHashMap<>();
                keywordCount.put(keyword, 1);
                keywordChartOptionMap.put(combinedCategory, keywordCount);
            } else {
                if (keywordCount.get(keyword) == null) {
                    keywordCount.put(keyword, 1);
                } else {
                    keywordCount.put(keyword, keywordCount.get(keyword) + 1);
                }
            }
        }
    }

    public ChartOptionDto getTopKeywordByCategory(String category, int topK) {
        String combinedCategory = Constant.combinedCategoryMap.getOrDefault(category, category);
        Map<String, Integer> keywordCount = keywordChartOptionMap.get(combinedCategory);
        if (keywordCount == null || keywordCount.isEmpty()) {
            return null;
        }
        PriorityQueue<Map.Entry<String, Integer>> heap = new PriorityQueue<>(
                Comparator.comparingInt(Map.Entry::getValue));
        for (Map.Entry<String, Integer> entry: keywordCount.entrySet()) {
            heap.offer(entry);
            if (heap.size() > topK) {
                heap.poll();
            }
        }
        List<String> keywords = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        while (!heap.isEmpty()) {
            Map.Entry<String, Integer> entry = heap.poll();
            keywords.add(entry.getKey());
            counts.add(entry.getValue());
        }
        return new ChartOptionDto(keywords, counts, combinedCategory);
    }

    public boolean pendingEmpty() {
        return pendingList.isEmpty();
    }

    public int pendingSize() {
        return pendingList.size();
    }
}
