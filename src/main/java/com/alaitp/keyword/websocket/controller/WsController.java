package com.alaitp.keyword.websocket.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Slf4j
@Controller
public class WsController {

    //映射客户端"/hello"请求
    @MessageMapping(value = "/keyword")
    //向订阅了"/topic/hello"主题的客户端广播消息
    @SendTo(value = "/topic/keyword")
    public String responseMsg(String msg) {  //msg->客户端传来的消息
        return msg + "world";
    }

//    @Scheduled(fixedRate = 1000*10) //设置定时器，每隔10s主动向客户端(订阅了"/topic/hello"主题)发送消息
//    @SendTo("/topic/hello")
//    public void scheduleSendMsg() {
//        Date now = new Date();
//        messagingTemplate.convertAndSend("/topic/hello", df.format(now));
//        log.info(now.toString());
//    }
//    //点对点通信
//    @Scheduled(fixedRate = 1000*10)
//    public void scheduleSendMsgToUser() {
//        Date now = new Date();
//        int userId = 1;
//        messagingTemplate.convertAndSendToUser(userId+"","/queue/hello", df.format(now));
//        log.info(now.toString());
//    }
}
