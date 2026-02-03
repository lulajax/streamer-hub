package com.mca.server.controller;

import com.mca.server.dto.ApiResponse;
import com.mca.server.dto.PresetDTO;
import com.mca.server.dto.SessionDTO;
import com.mca.server.dto.WidgetDataDTO;
import com.mca.server.service.PresetService;
import com.mca.server.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/widget")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WidgetController {

    private final PresetService presetService;
    private final SessionService sessionService;
    @Value("${mca.widget.base-url:http://localhost:3000}")
    private String widgetBaseUrl;

    /**
     * 渲染挂件页面（用于OBS嵌入）
     */
    @GetMapping("/{token}")
    public ResponseEntity<Void> renderWidget(
            @PathVariable String token,
            @RequestParam(defaultValue = "preset") String mode) {

        try {
            if ("session".equals(mode)) {
                sessionService.getSessionByWidgetToken(token);
            } else {
                presetService.getPresetByWidgetToken(token);
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }

        String apiBase = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();

        String redirectUrl = UriComponentsBuilder.fromUriString(widgetBaseUrl)
                .queryParam("token", token)
                .queryParam("mode", mode)
                .queryParam("apiBase", apiBase)
                .build()
                .toUriString();

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }

    /**
     * 获取挂件数据API（用于动态刷新）
     */
    @GetMapping("/{token}/data")
    public ResponseEntity<ApiResponse<WidgetDataDTO>> getWidgetData(
            @PathVariable String token,
            @RequestParam(defaultValue = "preset") String mode) {

        WidgetDataDTO data;
        if ("session".equals(mode)) {
            SessionDTO session = sessionService.getSessionByWidgetToken(token);
            data = buildSessionWidgetData(session);
        } else {
            PresetDTO preset = presetService.getPresetByWidgetToken(token);
            data = buildPresetWidgetData(preset);
        }

        return ResponseEntity.ok(ApiResponse.success(data));
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
