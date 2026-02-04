package com.mca.server.controller;

import com.mca.server.dto.ApiResponse;
import com.mca.server.websocket.RoomWebSocketHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "WebSocket监控", description = "WebSocket连接状态和统计信息接口")
@SecurityRequirement(name = "bearerAuth")
public class WebSocketMonitorController {
    
    private final RoomWebSocketHandler roomWebSocketHandler;
    
    @Operation(summary = "获取WebSocket统计", description = "获取所有房间的WebSocket连接统计信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = roomWebSocketHandler.getRoomStats();
        stats.put("totalConnections", roomWebSocketHandler.getTotalConnections());

        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    @Operation(summary = "获取房间状态", description = "获取指定房间的WebSocket连接状态")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "房间ID无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "房间不存在或没有活跃连接"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoomStatus(
            @Parameter(description = "房间ID", required = true, example = "room_abc123") @PathVariable String roomId) {
        Map<String, Object> allStats = roomWebSocketHandler.getRoomStats();
        Map<String, Object> roomStats = (Map<String, Object>) allStats.get(roomId);

        if (roomStats == null) {
            return ResponseEntity.ok(ApiResponse.error("Room not found or no active connections"));
        }

        roomStats.put("roomId", roomId);
        return ResponseEntity.ok(ApiResponse.success(roomStats));
    }
}


