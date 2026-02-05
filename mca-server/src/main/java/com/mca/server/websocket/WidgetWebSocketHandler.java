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
    private static final String ATTR_VIEW = "widget_view";
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

        String view = resolveView(session.getUri());
        session.getAttributes().put(ATTR_TOKEN, token);
        session.getAttributes().put(ATTR_VIEW, view);

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

        Map<String, WidgetPayload> payloadByView = new ConcurrentHashMap<>();

        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                continue;
            }

            String view = (String) session.getAttributes().get(ATTR_VIEW);
            WidgetPayload payload = payloadByView.computeIfAbsent(view, key -> buildPayload(token, key));
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
        String view = (String) session.getAttributes().get(ATTR_VIEW);

        try {
            WidgetPayload payload = buildPayload(token, view);
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

    private WidgetPayload buildPayload(String token, String view) {
        try {
            WidgetDataDTO data = widgetDataService.getWidgetDataByToken(token, view);
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

    private String resolveView(URI uri) {
        String view = "preview";
        if (uri != null && uri.getQuery() != null) {
            String[] pairs = uri.getQuery().split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2 && "view".equals(kv[0])) {
                    view = kv[1];
                    break;
                }
            }
        }

        if ("live".equals(view)) {
            return "live";
        }

        return "preview";
    }
}
