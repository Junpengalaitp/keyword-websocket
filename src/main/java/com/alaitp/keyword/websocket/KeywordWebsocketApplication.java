package com.alaitp.keyword.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableScheduling
@EnableDiscoveryClient
@SpringBootApplication
public class KeywordWebsocketApplication {
    public static void main(String[] args) {
        initLogging();
        SpringApplication.run(KeywordWebsocketApplication.class, args);
    }

    private static void initLogging() {
        String javaVersion = System.getProperty("java.version", "not specified");
        String osName = System.getProperty("os.name", "not specified");
        String userHome = System.getProperty("user.home", "not specified");
        String javaHome = System.getProperty("java.home", "not specified");
        log.info("** Your OS name is : " + osName);
        log.info("** The version of the JVM you are running is : " + javaVersion);
        log.info("** Your user home directory is : " + userHome);
        log.info("** Your JRE installation directory is : " + javaHome);
    }
}
