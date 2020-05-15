package com.alaitp.keyword.websocket.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MsgReceiver {
    @RabbitListener(queues = "${keyword.queue}")
    public void onMessage(String Msg) {
        log.info("received message: {}", Msg);
    }
}
