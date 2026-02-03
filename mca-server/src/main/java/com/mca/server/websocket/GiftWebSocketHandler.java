package com.mca.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mca.server.dto.GiftRecordDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class GiftWebSocketHandler extends TextWebSocketHandler {
    
    private final ObjectMapper objectMapper;
    
    // Store sessions by room ID
    private final Map<String, CopyOnWriteArrayList<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = getRoomId(session);
        roomSessions.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>()).add(session);
        log.info("WebSocket connection established for room: {}, session: {}", roomId, session.getId());
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomId = getRoomId(session);
        CopyOnWriteArrayList<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(roomId);
            }
        }
        log.info("WebSocket connection closed for room: {}, session: {}", roomId, session.getId());
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle incoming messages (e.g., client subscribing to a room)
        String payload = message.getPayload();
        log.debug("Received message: {}", payload);
    }
    
    public void broadcastGift(String roomId, GiftRecordDTO gift) {
        CopyOnWriteArrayList<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        
        try {
            String message = objectMapper.writeValueAsString(Map.of(
                "type", "gift",
                "data", gift
            ));
            
            TextMessage textMessage = new TextMessage(message);
            
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        log.error("Failed to send message to session: {}", session.getId(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to broadcast gift", e);
        }
    }
    
    public void broadcastLeaderboardUpdate(String roomId, Object leaderboardData) {
        CopyOnWriteArrayList<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        
        try {
            String message = objectMapper.writeValueAsString(Map.of(
                "type", "leaderboard",
                "data", leaderboardData
            ));
            
            TextMessage textMessage = new TextMessage(message);
            
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        log.error("Failed to send message to session: {}", session.getId(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to broadcast leaderboard update", e);
        }
    }
    
    public void broadcastSessionUpdate(String roomId, Object sessionData) {
        CopyOnWriteArrayList<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        
        try {
            String message = objectMapper.writeValueAsString(Map.of(
                "type", "session",
                "data", sessionData
            ));
            
            TextMessage textMessage = new TextMessage(message);
            
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        log.error("Failed to send message to session: {}", session.getId(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to broadcast session update", e);
        }
    }
    
    private String getRoomId(WebSocketSession session) {
        String path = session.getUri().getPath();
        // Extract room ID from path like /ws/gifts/{roomId}
        String[] parts = path.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : "default";
    }
}
