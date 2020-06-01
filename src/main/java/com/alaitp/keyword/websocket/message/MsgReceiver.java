package com.alaitp.keyword.websocket.message;

import com.alaitp.keyword.websocket.cache.KeywordCache;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class MsgReceiver {
    @Value("${pub-sub.destination.prefix}")
    private String pubSubDestinationPrefix;
    @Value("${keyword.destination}")
    private String keywordDestination;
    @Value("${available.keyword.category}")
    private String categories;
    private Set<String> availableCategories;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private KeywordCache keywordCache;

    @PostConstruct
    void init() {
        availableCategories = new HashSet<>();
        availableCategories.addAll(Arrays.asList(categories.split(",")));
    }

    @RabbitListener(queues = "${keyword.queue}")
    public void onMessage(String msg) {
        log.info("received message: {}", msg);
        JobKeywordDto jobKeywordDto = JSON.parseObject(msg, JobKeywordDto.class);
        keywordCache.addKeyword(jobKeywordDto);
        for (String category: jobKeywordDto.categories()) {
            if (availableCategories.contains(category)) {
                Map<String, Object[]> res = keywordCache.getTopKeywordByCategory(category);
                JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(res));
                jsonObject.put("category", category);
                messagingTemplate.convertAndSend(pubSubDestinationPrefix + keywordDestination, jsonObject);
                log.info("sent message: {}", jsonObject);
            }
        }
    }
}
