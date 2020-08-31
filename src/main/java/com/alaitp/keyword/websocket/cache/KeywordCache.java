package com.alaitp.keyword.websocket.cache;

import com.alaitp.keyword.websocket.constant.Constant;
import com.alaitp.keyword.websocket.dto.ChartOptionDto;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import com.alaitp.keyword.websocket.message.ChartOptionSession;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * store job keywords to calculate chart options data for front end
 *
 * @see ChartOptionSession
 */
@Slf4j
public class KeywordCache {
    /**
     * when receiving JobKeywordDto out of sending interval, add to pending list and use them in the next sending interval
     */
    private final Queue<JobKeywordDto> pendingList = new LinkedList<>();
    /**
     * the map used by heap to get top K chart options
     */
    private final ConcurrentMap<String, ConcurrentMap<String, Integer>> keywordChartOptionMap = new ConcurrentHashMap<>();
    /**
     * use binary heap(min heap) to find the top K frequent keywords for the category.
     */
    private final PriorityQueue<Map.Entry<String, Integer>> minHeap = new PriorityQueue<>(
            Comparator.comparingInt(Map.Entry::getValue));

    public void addPendingKeyword(JobKeywordDto jobKeywordDto) {
        pendingList.offer(jobKeywordDto);
    }

    /**
     * process job keywords in the queue, place them in the map for the heap
     */
    public void processPendingJobKeyword() {
        JobKeywordDto jobKeywordDto = pendingList.remove();
        addJobKeywordByCategory(jobKeywordDto);
    }

    /**
     * add individual keyword to the category map for generating top 10 frequent keywords for each category
     */
    public void addJobKeywordByCategory(JobKeywordDto jobKeywordDto) {
        List<JobKeywordDto.KeywordDto> keywordDtoList = jobKeywordDto.getKeywordList();
        for (JobKeywordDto.KeywordDto keywordDto : keywordDtoList) {
            String category = keywordDto.getCategory();
            String combinedCategory = Constant.combinedCategoryMap.getOrDefault(category, category);
            String keyword = keywordDto.getKeyword();
            keywordChartOptionMap.computeIfAbsent(combinedCategory, k -> new ConcurrentHashMap<>());
            ConcurrentMap<String, Integer> keywordCount = keywordChartOptionMap.get(combinedCategory);
            keywordCount.merge(keyword, 1, Integer::sum);
        }
    }

    /**
     * when the heap has more than top K items, call poll method to remove the smallest one, so at the last, top K items
     * will remain in the heap, and the time complexity will be log(K)
     */
    public ChartOptionDto getTopKeywordByCategory(String category, int topK) {
        String combinedCategory = Constant.combinedCategoryMap.getOrDefault(category, category);
        Map<String, Integer> keywordCount = keywordChartOptionMap.get(combinedCategory);
        if (keywordCount == null || keywordCount.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, Integer> entry : keywordCount.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > topK) {
                minHeap.poll();
            }
        }
        // only top K remains in the heap
        List<String> keywords = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        while (!minHeap.isEmpty()) {
            Map.Entry<String, Integer> entry = minHeap.poll();
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
