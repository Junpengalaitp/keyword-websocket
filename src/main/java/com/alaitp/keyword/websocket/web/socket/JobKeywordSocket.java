package com.alaitp.keyword.websocket.web.socket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class JobKeywordSocket extends TextWebSocketHandler {
    private static List<WebSocketSession> sessionList = new CopyOnWriteArrayList<>();

    public static List<WebSocketSession> getSessions() {
        return sessionList;
    }


    /**
     * send or receive a message to browser
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws IOException {
        log.info("received socket message >> " + message.getPayload());
        if (session != null && session.isOpen()) { //有session且open状态
            session.sendMessage(new TextMessage("received socket message"));
        }
    }

    /**
     * Send the message to all users
     */
    public static void sendMessageToBrowser(TextMessage message) {
        try {
            List<WebSocketSession> sessions = getSessions();
            if (sessions != null && !sessions.isEmpty()) {
                for (WebSocketSession session : sessions) {
                    if (session != null && session.isOpen()) {
                        synchronized (session) {
                            session.sendMessage(message);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Exception occurred", e);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        if (log.isDebugEnabled()) {
            log.debug("connect to the websocket success......");
        }
        sessionList.add(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        if (session.isOpen()) {
            session.close();
        }
        if (log.isDebugEnabled()) {
            log.debug("websocket connection closed......");
        }
        sessionList.remove(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        if (log.isDebugEnabled()) {
            log.debug("websocket connection closed......");
        }
        sessionList.remove(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
