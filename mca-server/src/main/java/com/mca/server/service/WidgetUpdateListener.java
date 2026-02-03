package com.mca.server.service;

import com.mca.server.dto.SessionDTO;
import com.mca.server.dto.PresetDTO;
import com.mca.server.event.WidgetUpdateEvent;
import com.mca.server.websocket.WidgetWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WidgetUpdateListener {

    private final WidgetWebSocketHandler widgetWebSocketHandler;
    private final SessionService sessionService;
    private final PresetService presetService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onWidgetUpdate(WidgetUpdateEvent event) {
        if (event.getToken() != null) {
            widgetWebSocketHandler.pushUpdateForToken(event.getToken());
            return;
        }

        if (event.getSessionId() != null) {
            try {
                SessionDTO session = sessionService.getSession(event.getSessionId());
                widgetWebSocketHandler.pushUpdateForToken(session.getWidgetToken());
            } catch (Exception e) {
                log.debug("Failed to push widget update for session {}", event.getSessionId(), e);
            }
            return;
        }

        if (event.getRoomId() != null) {
            pushForRoom(event.getRoomId());
            return;
        }

        if (event.getPresetId() != null) {
            try {
                PresetDTO preset = presetService.getPreset(event.getPresetId());
                widgetWebSocketHandler.pushUpdateForToken(preset.getWidgetToken());
            } catch (Exception e) {
                log.debug("Failed to push widget update for preset {}", event.getPresetId(), e);
            }
        }
    }

    private void pushForRoom(String roomId) {
        SessionDTO session = sessionService.getActiveSession(roomId);
        if (session != null) {
            widgetWebSocketHandler.pushUpdateForToken(session.getWidgetToken());
            return;
        }

        List<SessionDTO> sessions = sessionService.getRoomSessions(roomId);
        if (!sessions.isEmpty()) {
            widgetWebSocketHandler.pushUpdateForToken(sessions.get(0).getWidgetToken());
        }
    }
}
