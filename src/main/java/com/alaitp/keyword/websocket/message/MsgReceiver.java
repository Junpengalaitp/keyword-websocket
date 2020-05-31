package com.alaitp.keyword.websocket.message;

import com.alaitp.keyword.websocket.cache.KeywordCache;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class MsgReceiver {
    @Value("${pub-sub.destination.prefix}")
    private String pubSubDestinationPrefix;
    @Value("${keyword.destination}")
    private String keywordDestination;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private KeywordCache keywordCache;

    @RabbitListener(queues = "${keyword.queue}")
    public void onMessage(String msg) {
        log.info("received message: {}", msg);
        JobKeywordDto jobKeywordDto = JSON.parseObject(msg, JobKeywordDto.class);
        keywordCache.addKeyword(jobKeywordDto);
        Map<String, Object[]> res = keywordCache.getTopKeywordByCategory("PROGRAMMING_LANGUAGE");
        messagingTemplate.convertAndSend(pubSubDestinationPrefix + keywordDestination, res);
        log.info("sent message: {}", res);
    }
}
