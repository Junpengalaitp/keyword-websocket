package com.alaitp.keyword.websocket.service.impl;

import com.alaitp.keyword.websocket.service.MsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class MsgServiceImpl implements MsgService {
    @Value("${available.keyword.category}")
    private String categories;
    private Set<String> availableCategories;

    @PostConstruct
    void init() {
        availableCategories = new HashSet<>();
        availableCategories.addAll(Arrays.asList(categories.split(",")));
    }

//    @Override
//    public List<ChartOptionDto> getChartOptions() {
//        List<ChartOptionDto> chartOptionDtos = new ArrayList<>();
//        for (String category: availableCategories) {
//            ChartOptionDto chartOptionDto = keywordCache.getTopKeywordByCategory(category, 10);
//            chartOptionDtos.add(chartOptionDto);
//        }
//        return chartOptionDtos;
//    }
}
