FROM amazoncorretto:11-al2-full
VOLUME /tmp
COPY target/keyword-websocket-0.0.1-SNAPSHOT.jar keyword-websocket.jar
ENTRYPOINT ["java", "-jar", "keyword-websocket.jar"]