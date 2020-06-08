package com.alaitp.keyword.websocket.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {
    @Value("${keyword.queue}")
    private String keywordTopic;

    @Value("${keyword.exchange}")
    private String keywordExchange;

    @Value("${keyword.key}")
    private String keywordKey;

    @Bean
    public DirectExchange keywordExchange() {
        return new DirectExchange(keywordExchange);
    }

    @Bean
    public Queue keywordQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 10000);
        return new Queue(keywordTopic, true, false, false, args);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(keywordQueue()).to(keywordExchange()).with(keywordKey);
    }
}
