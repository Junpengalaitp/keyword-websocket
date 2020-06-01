package com.alaitp.keyword.websocket.cache;

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
            String keyword = keywordDto.getKeyword();
            Map<String, Integer> keywordCount = keywordCategoryMap.get(category);
            if (keywordCount == null) {
                keywordCount = new HashMap<>();
                keywordCount.put(keyword, 1);
                keywordCategoryMap.put(category, keywordCount);
            } else {
                if (keywordCount.get(keyword) == null) {
                    keywordCount.put(keyword, 1);
                } else {
                    keywordCount.put(keyword, keywordCount.get(keyword) + 1);
                }
            }
        }
    }
    public Map<String, Object[]> getTopKeywordByCategory(String category) {
        Map<String, Object[]> res = new HashMap<>();
        Map<String, Integer> keywordCount = keywordCategoryMap.get(category);
        if (keywordCount == null || keywordCount.isEmpty()) {
            return res;
        }
        Map<String, Integer> topFive = keywordCount.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        Object[] keywords = topFive.keySet().toArray();
        ArrayUtils.reverse(keywords);
        Object[] counts = topFive.values().toArray();
        ArrayUtils.reverse(counts);
        res.put("keyword", keywords);
        res.put("count", counts);
        return res;
    }
}
