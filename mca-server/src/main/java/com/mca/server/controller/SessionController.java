package com.mca.server.controller;

import com.mca.server.dto.ApiResponse;
import com.mca.server.dto.SessionDTO;
import com.mca.server.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SessionController {
    
    private final SessionService sessionService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<SessionDTO>> createSession(
            @RequestParam String roomId,
            @RequestParam String presetId) {
        SessionDTO session = sessionService.createSession(roomId, presetId);
        return ResponseEntity.ok(ApiResponse.success("Session created", session));
    }
    
    @PostMapping("/{sessionId}/start")
    public ResponseEntity<ApiResponse<SessionDTO>> startSession(
            @PathVariable String sessionId) {
        SessionDTO session = sessionService.startSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session started", session));
    }
    
    @PostMapping("/{sessionId}/pause")
    public ResponseEntity<ApiResponse<SessionDTO>> pauseSession(
            @PathVariable String sessionId) {
        SessionDTO session = sessionService.pauseSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session paused", session));
    }
    
    @PostMapping("/{sessionId}/end")
    public ResponseEntity<ApiResponse<SessionDTO>> endSession(
            @PathVariable String sessionId) {
        SessionDTO session = sessionService.endSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session ended", session));
    }
    
    @PostMapping("/{sessionId}/next-round")
    public ResponseEntity<ApiResponse<SessionDTO>> nextRound(
            @PathVariable String sessionId) {
        SessionDTO session = sessionService.nextRound(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Advanced to next round", session));
    }
    
    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<SessionDTO>> getSession(
            @PathVariable String sessionId) {
        SessionDTO session = sessionService.getSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success(session));
    }
    
    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<List<SessionDTO>>> getRoomSessions(
            @PathVariable String roomId) {
        List<SessionDTO> sessions = sessionService.getRoomSessions(roomId);
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }
    
    @GetMapping("/room/{roomId}/active")
    public ResponseEntity<ApiResponse<SessionDTO>> getActiveSession(
            @PathVariable String roomId) {
        SessionDTO session = sessionService.getActiveSession(roomId);
        return ResponseEntity.ok(ApiResponse.success(session));
    }

    @PostMapping("/quick-start")
    public ResponseEntity<ApiResponse<SessionDTO>> quickStart(
            @RequestParam String roomId,
            @RequestHeader("X-Device-Id") String deviceId) {
        SessionDTO session = sessionService.quickStartWithDefault(roomId, deviceId);
        return ResponseEntity.ok(ApiResponse.success("Quick started", session));
    }

    @GetMapping("/{sessionId}/widget-url")
    public ResponseEntity<ApiResponse<String>> getWidgetUrl(
            @PathVariable String sessionId) {
        String widgetUrl = sessionService.getWidgetUrl(sessionId);
        return ResponseEntity.ok(ApiResponse.success(widgetUrl));
    }
}
