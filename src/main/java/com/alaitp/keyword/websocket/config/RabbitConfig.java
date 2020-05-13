package com.alaitp.keyword.websocket.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Value("${keyword.topic}")
    private String keywordTopic;
    @Bean
    public Queue queue() {
        return new Queue(keywordTopic);
    }
}
