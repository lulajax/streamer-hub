package com.mca.server.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mca.server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;

/**
 * WebSocket Handler for Room State Management
 * 
 * Roles:
 * - producer: Electron client (only 1 per room)
 * - consumer: Widget/Browser source (multiple per room)
 * 
 * Message Types:
 * - join: Initial connection with roomId, role, token
 * - state: State snapshot from producer
 * - event: One-time event from producer
 * - snapshot: Full state sent to consumer on join/reconnect
 * - heartbeat: Keep-alive ping/pong
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoomWebSocketHandler extends TextWebSocketHandler {
    
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    
    // Room management - thread safe
    // roomId -> ProducerSession (only one producer per room)
    private final Map<String, WebSocketSession> roomProducerMap = new ConcurrentHashMap<>();
    
    // roomId -> Set of ConsumerSessions
    private final Map<String, Set<WebSocketSession>> roomConsumerMap = new ConcurrentHashMap<>();
    
    // roomId -> Latest State
    private final Map<String, JsonNode> latestStateMap = new ConcurrentHashMap<>();
    
    // roomId -> Sequence number counter
    private final Map<String, AtomicLong> seqCounterMap = new ConcurrentHashMap<>();
    
    // Session attributes
    private static final String ATTR_ROOM_ID = "roomId";
    private static final String ATTR_ROLE = "role";
    private static final String ATTR_JOINED = "joined";
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}, URI: {}", 
                session.getId(), session.getUri());
        
        // Set heartbeat interval
        session.setTextMessageSizeLimit(65536);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomId = (String) session.getAttributes().get(ATTR_ROOM_ID);
        String role = (String) session.getAttributes().get(ATTR_ROLE);
        
        log.info("WebSocket connection closed: {}, room: {}, role: {}, status: {}",
                session.getId(), roomId, role, status);
        
        if (roomId != null && role != null) {
            cleanupSession(session, roomId, role);
        }
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        
        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            String type = jsonNode.has("type") ? jsonNode.get("type").asText() : "";
            
            switch (type) {
                case "join":
                    handleJoin(session, jsonNode);
                    break;
                case "state":
                    handleState(session, jsonNode);
                    break;
                case "event":
                    handleEvent(session, jsonNode);
                    break;
                case "heartbeat":
                    handleHeartbeat(session, jsonNode);
                    break;
                default:
                    log.warn("Unknown message type: {} from session: {}", type, session.getId());
                    sendError(session, "Unknown message type: " + type);
            }
        } catch (Exception e) {
            log.error("Failed to handle message: {}", payload, e);
            sendError(session, "Invalid message format: " + e.getMessage());
        }
    }
    
    /**
     * Handle join message - first message after connection
     */
    private void handleJoin(WebSocketSession session, JsonNode message) {
        String roomId = message.has("roomId") ? message.get("roomId").asText() : null;
        String role = message.has("role") ? message.get("role").asText() : null;
        String token = message.has("token") ? message.get("token").asText() : null;
        
        // Validate required fields
        if (roomId == null || roomId.isEmpty()) {
            sendError(session, "Missing roomId");
            closeSession(session);
            return;
        }
        
        if (role == null || (!role.equals("producer") && !role.equals("consumer"))) {
            sendError(session, "Invalid or missing role. Must be 'producer' or 'consumer'");
            closeSession(session);
            return;
        }
        
        // Validate token
        if (!validateToken(token)) {
            sendError(session, "Invalid or expired token");
            closeSession(session);
            return;
        }
        
        // Store session attributes
        session.getAttributes().put(ATTR_ROOM_ID, roomId);
        session.getAttributes().put(ATTR_ROLE, role);
        session.getAttributes().put(ATTR_JOINED, true);
        
        if ("producer".equals(role)) {
            // Check if room already has a producer
            WebSocketSession existingProducer = roomProducerMap.get(roomId);
            if (existingProducer != null && existingProducer.isOpen()) {
                sendError(session, "Room already has a producer. Only one producer allowed per room.");
                closeSession(session);
                return;
            }
            
            // Register as producer
            roomProducerMap.put(roomId, session);
            
            // Initialize sequence counter
            seqCounterMap.putIfAbsent(roomId, new AtomicLong(0));
            
            log.info("Producer joined room: {}, session: {}", roomId, session.getId());
            
            // Send join success
            sendMessage(session, Map.of(
                "type", "join_success",
                "roomId", roomId,
                "role", "producer",
                "message", "Connected as producer"
            ));
            
        } else { // consumer
            // Add to consumer set
            roomConsumerMap.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(session);
            
            log.info("Consumer joined room: {}, session: {}, total consumers: {}", 
                    roomId, session.getId(), roomConsumerMap.get(roomId).size());
            
            // Send join success
            sendMessage(session, Map.of(
                "type", "join_success",
                "roomId", roomId,
                "role", "consumer",
                "message", "Connected as consumer"
            ));
            
            // Immediately send latest state if available
            JsonNode latestState = latestStateMap.get(roomId);
            if (latestState != null) {
                sendSnapshot(session, roomId, latestState);
            } else {
                // No producer yet
                sendMessage(session, Map.of(
                    "type", "waiting",
                    "roomId", roomId,
                    "message", "Waiting for producer to connect"
                ));
            }
        }
    }
    
    /**
     * Handle state message from producer
     */
    private void handleState(WebSocketSession session, JsonNode message) {
        // Verify sender is a joined producer
        if (!isValidProducer(session)) {
            sendError(session, "Not authorized to send state");
            return;
        }
        
        String roomId = (String) session.getAttributes().get(ATTR_ROOM_ID);
        
        // Get and increment sequence number
        AtomicLong seqCounter = seqCounterMap.get(roomId);
        if (seqCounter == null) {
            seqCounter = new AtomicLong(0);
            seqCounterMap.put(roomId, seqCounter);
        }
        long seq = seqCounter.incrementAndGet();
        
        // Add metadata to state
        ((com.fasterxml.jackson.databind.node.ObjectNode) message)
            .put("seq", seq)
            .put("ts", System.currentTimeMillis());
        
        // Update latest state
        latestStateMap.put(roomId, message);
        
        // Broadcast to all consumers
        broadcastToConsumers(roomId, message);
        
        log.debug("State broadcast to room: {}, seq: {}", roomId, seq);
    }
    
    /**
     * Handle event message from producer
     */
    private void handleEvent(WebSocketSession session, JsonNode message) {
        // Verify sender is a joined producer
        if (!isValidProducer(session)) {
            sendError(session, "Not authorized to send event");
            return;
        }
        
        String roomId = (String) session.getAttributes().get(ATTR_ROOM_ID);
        
        // Get sequence number
        AtomicLong seqCounter = seqCounterMap.get(roomId);
        if (seqCounter != null) {
            long seq = seqCounter.incrementAndGet();
            ((com.fasterxml.jackson.databind.node.ObjectNode) message)
                .put("seq", seq)
                .put("ts", System.currentTimeMillis());
        }
        
        // Broadcast event to all consumers
        broadcastToConsumers(roomId, message);
        
        log.debug("Event broadcast to room: {}", roomId);
    }
    
    /**
     * Handle heartbeat
     */
    private void handleHeartbeat(WebSocketSession session, JsonNode message) {
        // Echo back with server timestamp
        sendMessage(session, Map.of(
            "type", "heartbeat",
            "ts", System.currentTimeMillis()
        ));
    }
    
    /**
     * Send snapshot to a consumer
     */
    private void sendSnapshot(WebSocketSession session, String roomId, JsonNode state) {
        AtomicLong seqCounter = seqCounterMap.get(roomId);
        long seq = seqCounter != null ? seqCounter.get() : 0;
        
        Map<String, Object> snapshot = Map.of(
            "type", "snapshot",
            "roomId", roomId,
            "seq", seq,
            "ts", System.currentTimeMillis(),
            "payload", state.has("payload") ? state.get("payload") : state
        );
        
        sendMessage(session, snapshot);
        log.info("Snapshot sent to consumer: {}, room: {}", session.getId(), roomId);
    }
    
    /**
     * Broadcast message to all consumers in a room
     */
    private void broadcastToConsumers(String roomId, JsonNode message) {
        Set<WebSocketSession> consumers = roomConsumerMap.get(roomId);
        if (consumers == null || consumers.isEmpty()) {
            return;
        }
        
        String messageStr = message.toString();
        
        for (WebSocketSession consumer : consumers) {
            if (consumer.isOpen()) {
                try {
                    consumer.sendMessage(new TextMessage(messageStr));
                } catch (IOException e) {
                    log.error("Failed to send message to consumer: {}", consumer.getId(), e);
                }
            }
        }
    }
    
    /**
     * Validate if session is a valid joined producer
     */
    private boolean isValidProducer(WebSocketSession session) {
        Boolean joined = (Boolean) session.getAttributes().get(ATTR_JOINED);
        String role = (String) session.getAttributes().get(ATTR_ROLE);
        return joined != null && joined && "producer".equals(role);
    }
    
    /**
     * Validate JWT token
     */
    private boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        // For overlay tokens, we might have different validation
        // For now, use JWT validation
        return jwtUtil.validateToken(token);
    }
    
    /**
     * Cleanup session on disconnect
     */
    private void cleanupSession(WebSocketSession session, String roomId, String role) {
        if ("producer".equals(role)) {
            WebSocketSession currentProducer = roomProducerMap.get(roomId);
            if (currentProducer != null && currentProducer.getId().equals(session.getId())) {
                roomProducerMap.remove(roomId);
                
                // Notify all consumers that producer is offline
                broadcastToConsumers(roomId, objectMapper.valueToTree(Map.of(
                    "type", "producer_offline",
                    "roomId", roomId,
                    "message", "Producer disconnected"
                )));
                
                log.info("Producer removed from room: {}", roomId);
            }
        } else { // consumer
            Set<WebSocketSession> consumers = roomConsumerMap.get(roomId);
            if (consumers != null) {
                consumers.remove(session);
                if (consumers.isEmpty()) {
                    roomConsumerMap.remove(roomId);
                }
                log.info("Consumer removed from room: {}, remaining: {}", 
                        roomId, consumers.size());
            }
        }
    }
    
    /**
     * Send message to session
     */
    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
        if (!session.isOpen()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("Failed to send message to session: {}", session.getId(), e);
        }
    }
    
    /**
     * Send error message to session
     */
    private void sendError(WebSocketSession session, String error) {
        sendMessage(session, Map.of(
            "type", "error",
            "error", error
        ));
    }
    
    /**
     * Close session
     */
    private void closeSession(WebSocketSession session) {
        try {
            session.close(CloseStatus.BAD_DATA);
        } catch (IOException e) {
            log.error("Failed to close session: {}", session.getId(), e);
        }
    }
    
    // Public methods for monitoring/debugging
    
    public Map<String, Object> getRoomStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        for (String roomId : roomProducerMap.keySet()) {
            Map<String, Object> roomStat = new ConcurrentHashMap<>();
            roomStat.put("hasProducer", roomProducerMap.get(roomId) != null);
            roomStat.put("consumerCount", 
                    roomConsumerMap.getOrDefault(roomId, new CopyOnWriteArraySet<>()).size());
            roomStat.put("latestSeq", 
                    seqCounterMap.getOrDefault(roomId, new AtomicLong(0)).get());
            
            stats.put(roomId, roomStat);
        }
        
        return stats;
    }
    
    public int getTotalConnections() {
        int total = roomProducerMap.size();
        for (Set<WebSocketSession> consumers : roomConsumerMap.values()) {
            total += consumers.size();
        }
        return total;
    }
}
