package com.alaitp.keyword.websocket.service.impl;

import com.alaitp.keyword.websocket.cache.KeywordCache;
import com.alaitp.keyword.websocket.dto.ChartOptionDto;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import com.alaitp.keyword.websocket.dto.JobMsgDto;
import com.alaitp.keyword.websocket.service.MsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Slf4j
@Service
public class MsgServiceImpl implements MsgService {
    @Value("${available.keyword.category}")
    private String categories;
    private Set<String> availableCategories;
    @Autowired
    private KeywordCache keywordCache;

    @PostConstruct
    void init() {
        availableCategories = new HashSet<>();
        availableCategories.addAll(Arrays.asList(categories.split(",")));
    }

    @Override
    public JobMsgDto getJobKeywordMsg(JobKeywordDto jobKeywordDto) {
        List<ChartOptionDto> chartOptionDtos = new ArrayList<>();
        for (String category: availableCategories) {
            ChartOptionDto chartOptionDto = keywordCache.getTopKeywordByCategory(category);
            chartOptionDtos.add(chartOptionDto);
        }
        return new JobMsgDto(chartOptionDtos, jobKeywordDto);
    }
}
