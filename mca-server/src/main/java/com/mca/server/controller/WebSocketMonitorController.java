package com.mca.server.controller;

import com.mca.server.dto.ApiResponse;
import com.mca.server.websocket.RoomWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * WebSocket monitoring and management endpoints
 */
@RestController
@RequestMapping("/ws-monitor")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WebSocketMonitorController {
    
    private final RoomWebSocketHandler roomWebSocketHandler;
    
    /**
     * Get WebSocket connection statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = roomWebSocketHandler.getRoomStats();
        stats.put("totalConnections", roomWebSocketHandler.getTotalConnections());
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    /**
     * Get room connection status
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoomStatus(@PathVariable String roomId) {
        Map<String, Object> allStats = roomWebSocketHandler.getRoomStats();
        Map<String, Object> roomStats = (Map<String, Object>) allStats.get(roomId);
        
        if (roomStats == null) {
            return ResponseEntity.ok(ApiResponse.error("Room not found or no active connections"));
        }
        
        roomStats.put("roomId", roomId);
        return ResponseEntity.ok(ApiResponse.success(roomStats));
    }
}
