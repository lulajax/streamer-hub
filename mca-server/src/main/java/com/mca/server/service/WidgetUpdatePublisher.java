package com.mca.server.service;

import com.mca.server.event.WidgetUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WidgetUpdatePublisher {

    private final ApplicationEventPublisher publisher;

    public void publishForToken(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        publisher.publishEvent(WidgetUpdateEvent.builder().token(token).build());
    }

    public void publishForSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return;
        }
        publisher.publishEvent(WidgetUpdateEvent.builder().sessionId(sessionId).build());
    }

    public void publishForRoom(String roomId) {
        if (roomId == null || roomId.isEmpty()) {
            return;
        }
        publisher.publishEvent(WidgetUpdateEvent.builder().roomId(roomId).build());
    }

    public void publishForPreset(String presetId) {
        if (presetId == null || presetId.isEmpty()) {
            return;
        }
        publisher.publishEvent(WidgetUpdateEvent.builder().presetId(presetId).build());
    }
}
