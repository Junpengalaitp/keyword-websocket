package com.alaitp.keyword.websocket.cache;

import com.alaitp.keyword.websocket.constant.Constant;
import com.alaitp.keyword.websocket.dto.ChartOptionDto;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class KeywordCache {
    private Map<String, Map<String, Integer>> keywordCategoryMap = new HashMap<>();

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
    public ChartOptionDto getTopKeywordByCategory(String category) {
        String combinedCategory = Constant.combinedCategoryMap.getOrDefault(category, category);
        Map<String, Integer> keywordCount = keywordCategoryMap.get(combinedCategory);
        if (keywordCount == null || keywordCount.isEmpty()) {
            return null;
        }
        Map<String, Integer> topFive = keywordCount.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        Object[] keywords = topFive.keySet().toArray();
        ArrayUtils.reverse(keywords);
        Object[] counts = topFive.values().toArray();
        ArrayUtils.reverse(counts);
        return new ChartOptionDto(keywords, counts, combinedCategory);
    }
}
