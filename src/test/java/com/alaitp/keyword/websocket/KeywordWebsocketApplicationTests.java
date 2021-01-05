package com.alaitp.keyword.websocket;

import com.alaitp.keyword.websocket.constant.ConfigValue;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;


@SpringBootTest
class KeywordWebsocketApplicationTests {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ConfigValue configValue;

    @Test
    void contextLoads() {
    }

    @Test
    void sendMockMsg() {
        String msg = "{\"job_id\": \"remotive_117729\", \"keyword_list\": [{\"keyword\": \"best platform\", \"category\": \"APPROACH\", \"startIdx\": 47, \"endIdx\": 60}, {\"keyword\": \"Software Engineer\", \"category\": \"POSITION\", \"startIdx\": 78, \"endIdx\": 87}, {\"keyword\": \"Scalability\", \"category\": \"QUALITY\", \"startIdx\": 161, \"endIdx\": 176}, {\"keyword\": \"every day\", \"category\": \"DATE\", \"startIdx\": 223, \"endIdx\": 232}, {\"keyword\": \"tens of thousands\", \"category\": \"PERCENT\", \"startIdx\": 323, \"endIdx\": 340}, {\"keyword\": \"APM\", \"category\": \"GPE\", \"startIdx\": 474, \"endIdx\": 477}, {\"keyword\": \"monitoring\", \"category\": \"SOFTWARE_ENGINEERING\", \"startIdx\": 555, \"endIdx\": 565}, {\"keyword\": \"APM\", \"category\": \"OTHER_LANGUAGE\", \"startIdx\": 640, \"endIdx\": 643}, {\"keyword\": \"integration\", \"category\": \"SOFTWARE_ENGINEERING\", \"startIdx\": 682, \"endIdx\": 693}, {\"keyword\": \"Ruby\", \"category\": \"PROGRAMMING_LANGUAGE\", \"startIdx\": 789, \"endIdx\": 793}, {\"keyword\": \"Ruby\", \"category\": \"PROGRAMMING_LANGUAGE\", \"startIdx\": 820, \"endIdx\": 824}, {\"keyword\": \"thousands\", \"category\": \"CARDINAL\", \"startIdx\": 993, \"endIdx\": 1002}, {\"keyword\": \"Ruby\", \"category\": \"PROGRAMMING_LANGUAGE\", \"startIdx\": 1006, \"endIdx\": 1010}, {\"keyword\": \"Ruby\", \"category\": \"PROGRAMMING_LANGUAGE\", \"startIdx\": 1251, \"endIdx\": 1255}, {\"keyword\": \"Programmer\", \"category\": \"POSITION\", \"startIdx\": 1256, \"endIdx\": 1266}, {\"keyword\": \"GIL\", \"category\": \"ORG\", \"startIdx\": 1299, \"endIdx\": 1302}, {\"keyword\": \"Ruby than Rails\", \"category\": \"FRAMEWORK\", \"startIdx\": 1356, \"endIdx\": 1371}, {\"keyword\": \"Ruby\", \"category\": \"PROGRAMMING_LANGUAGE\", \"startIdx\": 1448, \"endIdx\": 1452}, {\"keyword\": \"Ruby on Rails\", \"category\": \"FRAMEWORK\", \"startIdx\": 1637, \"endIdx\": 1642}, {\"keyword\": \"Sinatra\", \"category\": \"PLATFORM\", \"startIdx\": 1644, \"endIdx\": 1651}, {\"keyword\": \"ActiveRecord\", \"category\": \"PRODUCT\", \"startIdx\": 1671, \"endIdx\": 1683}, {\"keyword\": \"MS\", \"category\": \"GPE\", \"startIdx\": 1760, \"endIdx\": 1762}, {\"keyword\": \"Work Experience\", \"category\": \"WORK_EXPERIENCE\", \"startIdx\": 1832, \"endIdx\": 1842}, {\"keyword\": \"JRuby\", \"category\": \"PROGRAMMING_LANGUAGE\", \"startIdx\": 1848, \"endIdx\": 1853}, {\"keyword\": \"JVM\", \"category\": \"PROGRAMMING_LANGUAGE\", \"startIdx\": 1878, \"endIdx\": 1881}, {\"keyword\": \"Work Experience\", \"category\": \"WORK_EXPERIENCE\", \"startIdx\": 1937, \"endIdx\": 1947}, {\"keyword\": \"Python\", \"category\": \"PROGRAMMING_LANGUAGE\", \"startIdx\": 1953, \"endIdx\": 1959}, {\"keyword\": \"Go\", \"category\": \"PROGRAMMING_LANGUAGE\", \"startIdx\": 1961, \"endIdx\": 1963}, {\"keyword\": \"Java\", \"category\": \"PROGRAMMING_LANGUAGE\", \"startIdx\": 1965, \"endIdx\": 1969}, {\"keyword\": \"JavaScript\", \"category\": \"PROGRAMMING_LANGUAGE\", \"startIdx\": 1971, \"endIdx\": 1981}, {\"keyword\": \"Ruby\", \"category\": \"PROGRAMMING_LANGUAGE\", \"startIdx\": 1985, \"endIdx\": 1989}], \"request_id\": \"c284cd70-8f5f-45b5-a459-97009d6e1ae4\"}\n";
        JobKeywordDto jobKeywordDto = JSON.parseObject(msg, JobKeywordDto.class);
        messagingTemplate.convertAndSend(configValue.keywordSendingDestination(), JSON.toJSONString(jobKeywordDto));
    }
}
