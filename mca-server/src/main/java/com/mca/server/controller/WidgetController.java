package com.mca.server.controller;

import com.mca.server.dto.ApiResponse;
import com.mca.server.dto.PresetDTO;
import com.mca.server.dto.SessionDTO;
import com.mca.server.entity.GiftRecord;
import com.mca.server.service.GiftService;
import com.mca.server.service.PresetService;
import com.mca.server.service.SessionService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/widget")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WidgetController {

    private final PresetService presetService;
    private final SessionService sessionService;
    private final GiftService giftService;

    /**
     * 渲染挂件页面（用于OBS嵌入）
     */
    @GetMapping("/{token}")
    public ResponseEntity<String> renderWidget(
            @PathVariable String token,
            @RequestParam(defaultValue = "preset") String mode) {

        WidgetData data;
        try {
            if ("session".equals(mode)) {
                SessionDTO session = sessionService.getSessionByWidgetToken(token);
                data = buildSessionWidgetData(session);
            } else {
                PresetDTO preset = presetService.getPresetByWidgetToken(token);
                data = buildPresetWidgetData(preset);
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }

        String html = generateWidgetHtml(data);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    /**
     * 获取挂件数据API（用于动态刷新）
     */
    @GetMapping("/{token}/data")
    public ResponseEntity<ApiResponse<WidgetData>> getWidgetData(
            @PathVariable String token,
            @RequestParam(defaultValue = "preset") String mode) {

        WidgetData data;
        if ("session".equals(mode)) {
            SessionDTO session = sessionService.getSessionByWidgetToken(token);
            data = buildSessionWidgetData(session);
        } else {
            PresetDTO preset = presetService.getPresetByWidgetToken(token);
            data = buildPresetWidgetData(preset);
        }

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    private WidgetData buildPresetWidgetData(PresetDTO preset) {
        return WidgetData.builder()
                .token(preset.getWidgetToken())
                .name(preset.getName())
                .gameMode(preset.getGameMode())
                .status("PREVIEW")
                .widgetSettings(preset.getWidgetSettingsJson())
                .anchors(preset.getAnchors())
                .build();
    }

    private WidgetData buildSessionWidgetData(SessionDTO session) {
        return WidgetData.builder()
                .token(session.getWidgetToken())
                .name(session.getPresetName())
                .gameMode(session.getGameMode())
                .status(session.getStatus().name())
                .currentRound(session.getCurrentRound())
                .totalGifts(session.getTotalGifts())
                .totalDiamonds(session.getTotalDiamonds())
                .widgetSettings(session.getWidgetSettingsSnapshot())
                .build();
    }

    private String generateWidgetHtml(WidgetData data) {
        String customStyles = generateCustomStyles(data.getWidgetSettings());

        return """<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MCA Widget</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', sans-serif;
            background: transparent;
            overflow: hidden;
        }
        #widget-root {
            width: 100%;
            height: 100vh;
        }
        ${customStyles}
    </style>
</head>
<body>
    <div id="widget-root"></div>
    <script>
        // Widget Configuration
        window.WIDGET_CONFIG = {
            token: '${token}',
            name: '${name}',
            gameMode: '${gameMode}',
            status: '${status}',
            mode: '${mode}'
        };

        // WebSocket Connection
        const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = wsProtocol + '//' + window.location.host + '/ws/widget/' + '${token}';

        let ws = null;
        let reconnectInterval = null;

        function connect() {
            ws = new WebSocket(wsUrl);

            ws.onopen = function() {
                console.log('Widget WebSocket connected');
                if (reconnectInterval) {
                    clearInterval(reconnectInterval);
                    reconnectInterval = null;
                }
            };

            ws.onmessage = function(event) {
                const data = JSON.parse(event.data);
                updateWidget(data);
            };

            ws.onclose = function() {
                console.log('Widget WebSocket disconnected');
                if (!reconnectInterval) {
                    reconnectInterval = setInterval(connect, 5000);
                }
            };

            ws.onerror = function(error) {
                console.error('Widget WebSocket error:', error);
            };
        }

        function updateWidget(data) {
            // Dispatch custom event for widget updates
            const event = new CustomEvent('widgetUpdate', { detail: data });
            window.dispatchEvent(event);
        }

        // Initialize
        connect();

        // Cleanup on page unload
        window.addEventListener('beforeunload', function() {
            if (ws) {
                ws.close();
            }
            if (reconnectInterval) {
                clearInterval(reconnectInterval);
            }
        });
    </script>
</body>
</html>"""
                .replace("${token}", data.getToken() != null ? data.getToken() : "")
                .replace("${name}", data.getName() != null ? data.getName() : "")
                .replace("${gameMode}", data.getGameMode() != null ? data.getGameMode().name() : "")
                .replace("${status}", data.getStatus() != null ? data.getStatus() : "")
                .replace("${mode}", data.getStatus().equals("PREVIEW") ? "preset" : "session")
                .replace("${customStyles}", customStyles);
    }

    private String generateCustomStyles(String widgetSettingsJson) {
        if (widgetSettingsJson == null || widgetSettingsJson.isEmpty()) {
            return "";
        }
        return """
        /* Custom Widget Styles */
        .widget-container {
            background: rgba(0, 0, 0, 0.8);
            border-radius: 8px;
            padding: 12px;
            color: #fff;
        }
        """;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WidgetData {
        private String token;
        private String name;
        private Preset.GameMode gameMode;
        private String status;
        private Integer currentRound;
        private Long totalGifts;
        private Long totalDiamonds;
        private String widgetSettings;
        private List<?> anchors;
    }
}
