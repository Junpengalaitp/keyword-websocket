package com.alaitp.keyword.websocket.message;

import com.alaitp.keyword.websocket.cache.KeywordCache;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import com.alaitp.keyword.websocket.dto.JobMsgDto;
import com.alaitp.keyword.websocket.service.MsgService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MsgReceiver {
    @Value("${pub-sub.destination.prefix}")
    private String pubSubDestinationPrefix;
    @Value("${keyword.destination}")
    private String keywordDestination;
    private final SimpMessagingTemplate messagingTemplate;
    private final KeywordCache keywordCache;
    private final MsgService msgService;

    public MsgReceiver(SimpMessagingTemplate messagingTemplate, KeywordCache keywordCache, MsgService msgService) {
        this.messagingTemplate = messagingTemplate;
        this.keywordCache = keywordCache;
        this.msgService = msgService;
    }

    @RabbitListener(queues = "${keyword.queue}")
    public void onMessage(String msg) {
        log.info("received message: {}", msg);
        JobKeywordDto jobKeywordDto = JSON.parseObject(msg, JobKeywordDto.class);
        keywordCache.addKeyword(jobKeywordDto);
        JobMsgDto jobMsgDto = msgService.getJobKeywordMsg(jobKeywordDto);
        messagingTemplate.convertAndSend(pubSubDestinationPrefix + keywordDestination, jobMsgDto);
        log.info("sent message: {}", jobMsgDto);
    }
}
