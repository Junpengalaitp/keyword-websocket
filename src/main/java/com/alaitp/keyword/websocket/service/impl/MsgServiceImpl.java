package com.alaitp.keyword.websocket.service.impl;

import com.alaitp.keyword.websocket.constant.Constant;
import com.alaitp.keyword.websocket.dto.ChartOptionDto;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import com.alaitp.keyword.websocket.service.MsgService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MsgServiceImpl implements MsgService {
    @Value("${pub-sub.destination.prefix}")
    private String pubSubDestinationPrefix;
    @Value("${keyword.destination}")
    private String keywordDestination;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendJobKeyword(JobKeywordDto jobKeywordDto) {
        JSONObject jobKeywordJson = JSON.parseObject(JSON.toJSONString(jobKeywordDto));
        jobKeywordJson.put("msgType", "jobKeyword");
        messagingTemplate.convertAndSend(pubSubDestinationPrefix + keywordDestination, jobKeywordJson);
    }

    @Override
    public void sendChartOptions(List<ChartOptionDto> chartOptionDtoList) {
        messagingTemplate.convertAndSend(Constant.pubSubDestinationPrefix + Constant.keywordDestination, chartOptionDtoList);
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
