package com.alaitp.keyword.websocket.message;

import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MsgReceiver {
    @RabbitListener(queues = "${keyword.queue}")
    public void onMessage(String msg) {
        JobKeywordDto jobKeywordDto = JSON.parseObject(msg, JobKeywordDto.class);
        log.info("received message: {}", jobKeywordDto);
    }
}
