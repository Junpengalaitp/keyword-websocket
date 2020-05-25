package com.alaitp.keyword.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableEurekaClient
@SpringBootApplication
public class KeywordWebsocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(KeywordWebsocketApplication.class, args);
    }

}
