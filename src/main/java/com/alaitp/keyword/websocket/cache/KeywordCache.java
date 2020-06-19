package com.alaitp.keyword.websocket.cache;

import com.alaitp.keyword.websocket.constant.Constant;
import com.alaitp.keyword.websocket.dto.ChartOptionDto;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * store job keywords to calculate chart options data for front end
 */
@Slf4j
@Component
public class KeywordCache {
    private final Map<String, Map<String, Integer>> keywordCategoryMap = new HashMap<>();

    private String requestId = null;

    public void addKeyword(JobKeywordDto jobKeywordDto) {
        if (!jobKeywordDto.getRequestId().equals(requestId)) {
            keywordCategoryMap.clear();
        }
        requestId = jobKeywordDto.getRequestId();

        List<JobKeywordDto.KeywordDto> keywordDtoList = jobKeywordDto.getKeywordList();
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
}
