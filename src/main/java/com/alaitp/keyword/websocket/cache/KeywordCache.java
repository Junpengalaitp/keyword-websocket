package com.alaitp.keyword.websocket.cache;

import com.alaitp.keyword.websocket.constant.Constant;
import com.alaitp.keyword.websocket.dto.ChartOptionDto;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * store job keywords to calculate chart options data for front end
 */
@Slf4j
public class KeywordCache {
    private final Map<String, Map<String, Integer>> keywordCategoryMap = new HashMap<>();
    // when receiving JobKeywordDto out of sending interval, add to pending list and use them in the next sending interval
    private List<JobKeywordDto> pendingList = new ArrayList<>();

    private int sendingInterval;
    private Long lastSentTime = null;

    public KeywordCache(int sendingInterval) {
        this.sendingInterval = sendingInterval;
    }

    public void addKeyword(JobKeywordDto jobKeywordDto) {
        pendingList.add(jobKeywordDto);
        if (lastSentTime != null && betweenInterval()) {
            return;
        }
        for (JobKeywordDto pendingJobKeywordDto: pendingList) {
            List<JobKeywordDto.KeywordDto> keywordDtoList = pendingJobKeywordDto.getKeywordList();
            for (JobKeywordDto.KeywordDto keywordDto : keywordDtoList) {
                String category = keywordDto.getCategory();
                String combinedCategory = Constant.combinedCategoryMap.getOrDefault(category, category);
                String keyword = keywordDto.getKeyword();
                Map<String, Integer> keywordCount = keywordCategoryMap.get(combinedCategory);
                if (keywordCount == null) {
                    keywordCount = new HashMap<>();
                    keywordCount.put(keyword, 1);
                    keywordCategoryMap.put(combinedCategory, keywordCount);
                } else {
                    if (keywordCount.get(keyword) == null) {
                        keywordCount.put(keyword, 1);
                    } else {
                        keywordCount.put(keyword, keywordCount.get(keyword) + 1);
                    }
                }
            }
        }
    }

    public ChartOptionDto getTopKeywordByCategory(String category, int topK) {
        String combinedCategory = Constant.combinedCategoryMap.getOrDefault(category, category);
        Map<String, Integer> keywordCount = keywordCategoryMap.get(combinedCategory);
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

    public boolean isEmpty() {
        if (keywordCategoryMap.isEmpty()) {
            return true;
        }
        for (Map<String, Integer> map: keywordCategoryMap.values()) {
            if (!map.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean betweenInterval() {
        return System.currentTimeMillis() - lastSentTime < this.sendingInterval;
    }
}
