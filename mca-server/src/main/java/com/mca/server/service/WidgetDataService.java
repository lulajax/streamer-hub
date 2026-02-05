package com.mca.server.service;

import com.mca.server.dto.PresetDTO;
import com.mca.server.dto.SessionDTO;
import com.mca.server.dto.WidgetDataDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WidgetDataService {

    private final PresetService presetService;
    private final SessionService sessionService;

    @Transactional(readOnly = true)
    public WidgetDataDTO getWidgetDataByToken(String token, String view) {
        PresetDTO preset = presetService.getPresetByWidgetToken(token);

        // view=live 返回实时会话数据
        if ("live".equals(view)) {
            Optional<SessionDTO> latestSession = sessionService.getLatestSessionByPresetId(preset.getId());
            if (latestSession.isPresent()) {
                WidgetDataDTO data = buildSessionWidgetData(latestSession.get());
                data.setToken(preset.getWidgetToken());
                return data;
            }
        }

        // 默认返回预览数据
        return buildPresetWidgetData(preset);
    }

    @Transactional(readOnly = true)
    public WidgetDataDTO getWidgetDataBySessionId(String sessionId) {
        SessionDTO session = sessionService.getSession(sessionId);
        return buildSessionWidgetData(session);
    }

    @Transactional(readOnly = true)
    public WidgetDataDTO getWidgetDataByPresetId(String presetId) {
        PresetDTO preset = presetService.getPreset(presetId);
        Optional<SessionDTO> latestSession = sessionService.getLatestSessionByPresetId(presetId);
        if (latestSession.isPresent()) {
            WidgetDataDTO data = buildSessionWidgetData(latestSession.get());
            data.setToken(preset.getWidgetToken());
            return data;
        }

        return buildPresetWidgetData(preset);
    }

    private WidgetDataDTO buildPresetWidgetData(PresetDTO preset) {
        return WidgetDataDTO.builder()
                .token(preset.getWidgetToken())
                .name(preset.getName())
                .gameMode(preset.getGameMode())
                .status("PREVIEW")
                .widgetSettings(preset.getWidgetSettingsJson())
                .anchors(preset.getAnchors())
                .build();
    }

    private WidgetDataDTO buildSessionWidgetData(SessionDTO session) {
        PresetDTO preset = null;
        if (session.getPresetId() != null) {
            preset = presetService.getPreset(session.getPresetId());
        }

        return WidgetDataDTO.builder()
                .token(session.getWidgetToken())
                .name(session.getPresetName())
                .gameMode(session.getGameMode())
                .status(session.getStatus().name())
                .currentRound(session.getCurrentRound())
                .totalGifts(session.getTotalGifts())
                .totalDiamonds(session.getTotalDiamonds())
                .widgetSettings(session.getWidgetSettingsSnapshot())
                .anchors(preset != null ? preset.getAnchors() : null)
                .build();
    }
}
