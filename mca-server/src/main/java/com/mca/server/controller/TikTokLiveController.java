package com.mca.server.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.mca.server.dto.ApiResponse;
import com.mca.server.service.TikTokLiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/tiktok")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TikTokLiveController {
    
    private final TikTokLiveService tikTokLiveService;
    
    /**
     * Check if a TikTok user is currently live
     */
    @GetMapping("/is-live/{uniqueId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> isUserLive(@PathVariable String uniqueId) {
        boolean isLive = tikTokLiveService.isUserLive(uniqueId);
        Map<String, Object> result = new HashMap<>();
        result.put("uniqueId", uniqueId);
        result.put("isLive", isLive);
        
        if (isLive) {
            String roomId = tikTokLiveService.getRoomId(uniqueId);
            result.put("roomId", roomId);
        }
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * Get room information for a TikTok user
     */
    @GetMapping("/room-info/{uniqueId}")
    public ResponseEntity<ApiResponse<JsonNode>> getRoomInfo(@PathVariable String uniqueId) {
        JsonNode roomInfo = tikTokLiveService.getRoomInfo(uniqueId);
        if (roomInfo == null) {
            return ResponseEntity.ok(ApiResponse.error("Failed to get room info or user is not live"));
        }
        return ResponseEntity.ok(ApiResponse.success(roomInfo));
    }
    
    /**
     * Get room ID for a TikTok user
     */
    @GetMapping("/room-id/{uniqueId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoomId(@PathVariable String uniqueId) {
        String roomId = tikTokLiveService.getRoomId(uniqueId);
        if (roomId == null) {
            return ResponseEntity.ok(ApiResponse.error("User is not live or room ID not found"));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("uniqueId", uniqueId);
        result.put("roomId", roomId);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * Get available gifts for a room
     */
    @GetMapping("/gifts/{roomId}")
    public ResponseEntity<ApiResponse<JsonNode>> getAvailableGifts(@PathVariable String roomId) {
        JsonNode gifts = tikTokLiveService.getAvailableGifts(roomId);
        return ResponseEntity.ok(ApiResponse.success(gifts));
    }
    
    /**
     * Get signed WebSocket URL for connecting to TikTok Live
     */
    @GetMapping("/websocket-url")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSignedWebSocketUrl(
            @RequestParam String uniqueId,
            @RequestParam(required = false) String roomId) {
        
        String websocketUrl = tikTokLiveService.getSignedWebSocketUrl(uniqueId, roomId);
        if (websocketUrl == null) {
            return ResponseEntity.ok(ApiResponse.error("Failed to get WebSocket URL"));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("uniqueId", uniqueId);
        result.put("roomId", roomId);
        result.put("websocketUrl", websocketUrl);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * Get complete connection info for a TikTok live stream
     */
    @GetMapping("/connect-info/{uniqueId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getConnectInfo(@PathVariable String uniqueId) {
        // Check if user is live
        if (!tikTokLiveService.isUserLive(uniqueId)) {
            return ResponseEntity.ok(ApiResponse.error("User is not currently live"));
        }
        
        // Get room info
        JsonNode roomInfo = tikTokLiveService.getRoomInfo(uniqueId);
        if (roomInfo == null) {
            return ResponseEntity.ok(ApiResponse.error("Failed to get room info"));
        }
        
        String roomId = tikTokLiveService.getRoomId(uniqueId);
        String websocketUrl = tikTokLiveService.getSignedWebSocketUrl(uniqueId, roomId);
        JsonNode gifts = tikTokLiveService.getAvailableGifts(roomId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("uniqueId", uniqueId);
        result.put("roomId", roomId);
        result.put("roomInfo", roomInfo);
        result.put("websocketUrl", websocketUrl);
        result.put("availableGifts", gifts);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
