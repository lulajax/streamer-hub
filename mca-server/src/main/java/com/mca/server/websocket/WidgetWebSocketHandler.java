package com.mca.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mca.server.dto.WidgetDataDTO;
import com.mca.server.service.WidgetDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WidgetWebSocketHandler extends TextWebSocketHandler {

    private static final String ATTR_LAST_PAYLOAD = "widget_last_payload";
    private static final String ATTR_LAST_DATA = "widget_last_data";
    private static final String ATTR_MODE = "widget_mode";
    private static final String ATTR_TOKEN = "widget_token";

    private final WidgetDataService widgetDataService;
    private final ObjectMapper objectMapper;

    private final Map<String, Set<WebSocketSession>> tokenSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = extractToken(session.getUri());
        if (token == null || token.isEmpty()) {
            sendError(session, "Missing widget token");
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        String mode = resolveMode(session.getUri(), token);
        session.getAttributes().put(ATTR_TOKEN, token);
        session.getAttributes().put(ATTR_MODE, mode);

        tokenSessions.computeIfAbsent(token, key -> ConcurrentHashMap.newKeySet()).add(session);
        sendWidgetUpdate(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String token = (String) session.getAttributes().get(ATTR_TOKEN);
        if (token != null) {
            Set<WebSocketSession> sessions = tokenSessions.get(token);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    tokenSessions.remove(token);
                }
            }
        }
    }

    public void pushUpdateForToken(String token) {
        Set<WebSocketSession> sessions = tokenSessions.get(token);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        Map<String, WidgetPayload> payloadByMode = new ConcurrentHashMap<>();

        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                continue;
            }

            String mode = (String) session.getAttributes().get(ATTR_MODE);
            WidgetPayload payload = payloadByMode.computeIfAbsent(mode, key -> buildPayload(token, key));
            if (payload == null) {
                continue;
            }

            try {
                String lastData = (String) session.getAttributes().get(ATTR_LAST_DATA);
                if (!payload.dataJson.equals(lastData)) {
                    session.getAttributes().put(ATTR_LAST_DATA, payload.dataJson);
                    session.getAttributes().put(ATTR_LAST_PAYLOAD, payload.payloadJson);
                    session.sendMessage(new TextMessage(payload.payloadJson));
                }
            } catch (Exception e) {
                log.warn("Failed to send widget update for token {}", token, e);
            }
        }
    }

    private void sendWidgetUpdate(WebSocketSession session) {
        if (!session.isOpen()) {
            return;
        }

        String token = (String) session.getAttributes().get(ATTR_TOKEN);
        String mode = (String) session.getAttributes().get(ATTR_MODE);

        try {
            WidgetPayload payload = buildPayload(token, mode);
            if (payload == null) {
                sendError(session, "Invalid widget token");
                session.close(CloseStatus.BAD_DATA);
                return;
            }
            String lastData = (String) session.getAttributes().get(ATTR_LAST_DATA);
            if (!payload.dataJson.equals(lastData)) {
                session.getAttributes().put(ATTR_LAST_DATA, payload.dataJson);
                session.getAttributes().put(ATTR_LAST_PAYLOAD, payload.payloadJson);
                session.sendMessage(new TextMessage(payload.payloadJson));
            }
        } catch (Exception e) {
            log.warn("Failed to send widget update for token {}", token, e);
            sendError(session, "Failed to load widget data");
        }
    }

    private WidgetPayload buildPayload(String token, String mode) {
        try {
            WidgetDataDTO data = widgetDataService.getWidgetDataByToken(token, mode);
            String dataJson = objectMapper.writeValueAsString(data);
            String payloadJson = objectMapper.writeValueAsString(Map.of(
                    "type", "widget_data",
                    "ts", System.currentTimeMillis(),
                    "payload", data
            ));
            return new WidgetPayload(payloadJson, dataJson);
        } catch (Exception e) {
            log.warn("Failed to build widget payload for token {}", token, e);
            return null;
        }
    }

    private static class WidgetPayload {
        private final String payloadJson;
        private final String dataJson;

        private WidgetPayload(String payloadJson, String dataJson) {
            this.payloadJson = payloadJson;
            this.dataJson = dataJson;
        }
    }

    private void sendError(WebSocketSession session, String error) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "type", "error",
                    "error", error
            ));
            session.sendMessage(new TextMessage(payload));
        } catch (Exception e) {
            log.warn("Failed to send error message", e);
        }
    }

    private String extractToken(URI uri) {
        if (uri == null) {
            return null;
        }
        String path = uri.getPath();
        if (path == null || path.isEmpty()) {
            return null;
        }
        int idx = path.lastIndexOf('/');
        if (idx == -1 || idx == path.length() - 1) {
            return null;
        }
        return path.substring(idx + 1);
    }

    private String resolveMode(URI uri, String token) {
        String mode = "preset";
        if (uri != null && uri.getQuery() != null) {
            String[] pairs = uri.getQuery().split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2 && "mode".equals(kv[0])) {
                    mode = kv[1];
                    break;
                }
            }
        }

        if ("session".equals(mode) || "preset".equals(mode)) {
            return mode;
        }

        if (token != null && token.startsWith("sess_")) {
            return "session";
        }

        return "preset";
    }
}
