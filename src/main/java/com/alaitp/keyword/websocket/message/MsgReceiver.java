package com.alaitp.keyword.websocket.message;

import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = "${keyword.queue}")
    public void onMessage(String msg) {
        JobKeywordDto jobKeywordDto = JSON.parseObject(msg, JobKeywordDto.class);
        messagingTemplate.convertAndSend(pubSubDestinationPrefix + keywordDestination, JSON.toJSONString(jobKeywordDto));
        log.info("received message: {}", jobKeywordDto);
    }
}
