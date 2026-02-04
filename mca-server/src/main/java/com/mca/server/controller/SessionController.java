package com.mca.server.controller;

import com.mca.server.dto.ApiResponse;
import com.mca.server.dto.SessionDTO;
import com.mca.server.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "会话管理", description = "直播会话的创建、控制和状态管理接口")
@SecurityRequirement(name = "bearerAuth")
public class SessionController {
    
    private final SessionService sessionService;
    
    @Operation(summary = "创建会话", description = "为指定房间创建新的直播会话")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "会话创建成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该房间"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "房间或预设不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<SessionDTO>> createSession(
            @Parameter(description = "房间ID", required = true, example = "room_abc123") @RequestParam String roomId,
            @Parameter(description = "预设ID", required = true, example = "preset_xyz789") @RequestParam String presetId) {
        SessionDTO session = sessionService.createSession(roomId, presetId);
        return ResponseEntity.ok(ApiResponse.success("Session created", session));
    }

    @Operation(summary = "开始会话", description = "开始指定的直播会话")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "会话开始成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "会话ID无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该会话"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "会话不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "会话状态不允许开始"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/{sessionId}/start")
    public ResponseEntity<ApiResponse<SessionDTO>> startSession(
            @Parameter(description = "会话ID", required = true, example = "sess_abc123") @PathVariable String sessionId) {
        SessionDTO session = sessionService.startSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session started", session));
    }

    @Operation(summary = "暂停会话", description = "暂停指定的直播会话")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "会话暂停成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "会话ID无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该会话"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "会话不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "会话状态不允许暂停"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/{sessionId}/pause")
    public ResponseEntity<ApiResponse<SessionDTO>> pauseSession(
            @Parameter(description = "会话ID", required = true, example = "sess_abc123") @PathVariable String sessionId) {
        SessionDTO session = sessionService.pauseSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session paused", session));
    }

    @Operation(summary = "结束会话", description = "结束指定的直播会话")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "会话结束成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "会话ID无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该会话"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "会话不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/{sessionId}/end")
    public ResponseEntity<ApiResponse<SessionDTO>> endSession(
            @Parameter(description = "会话ID", required = true, example = "sess_abc123") @PathVariable String sessionId) {
        SessionDTO session = sessionService.endSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session ended", session));
    }

    @Operation(summary = "进入下一轮", description = "将会话推进到下一轮")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "轮次推进成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "会话ID无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该会话"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "会话不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/{sessionId}/next-round")
    public ResponseEntity<ApiResponse<SessionDTO>> nextRound(
            @Parameter(description = "会话ID", required = true, example = "sess_abc123") @PathVariable String sessionId) {
        SessionDTO session = sessionService.nextRound(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Advanced to next round", session));
    }

    @Operation(summary = "获取会话详情", description = "获取指定会话的详细信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "会话ID无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该会话"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "会话不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<SessionDTO>> getSession(
            @Parameter(description = "会话ID", required = true, example = "sess_abc123") @PathVariable String sessionId) {
        SessionDTO session = sessionService.getSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success(session));
    }

    @Operation(summary = "获取房间会话列表", description = "获取指定房间的所有会话历史")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "房间ID无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该房间"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "房间不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<List<SessionDTO>>> getRoomSessions(
            @Parameter(description = "房间ID", required = true, example = "room_abc123") @PathVariable String roomId) {
        List<SessionDTO> sessions = sessionService.getRoomSessions(roomId);
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @Operation(summary = "获取活跃会话", description = "获取指定房间当前活跃的会话")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "房间ID无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该房间"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "房间不存在或没有活跃会话"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/room/{roomId}/active")
    public ResponseEntity<ApiResponse<SessionDTO>> getActiveSession(
            @Parameter(description = "房间ID", required = true, example = "room_abc123") @PathVariable String roomId) {
        SessionDTO session = sessionService.getActiveSession(roomId);
        return ResponseEntity.ok(ApiResponse.success(session));
    }

    @Operation(summary = "快速开始", description = "使用默认配置快速开始一个会话")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "快速开始成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "设备没有默认配置"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该设备"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "房间不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/quick-start")
    public ResponseEntity<ApiResponse<SessionDTO>> quickStart(
            @Parameter(description = "房间ID", required = true, example = "room_abc123") @RequestParam String roomId,
            @Parameter(description = "设备ID", required = true, example = "device_abc123_xyz")
            @RequestHeader("X-Device-Id") String deviceId) {
        SessionDTO session = sessionService.quickStartWithDefault(roomId, deviceId);
        return ResponseEntity.ok(ApiResponse.success("Quick started", session));
    }

    @Operation(summary = "获取挂件链接", description = "获取会话的挂件OBS嵌入链接")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "会话ID无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该会话"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "会话不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/{sessionId}/widget-url")
    public ResponseEntity<ApiResponse<String>> getWidgetUrl(
            @Parameter(description = "会话ID", required = true, example = "sess_abc123") @PathVariable String sessionId) {
        String widgetUrl = sessionService.getWidgetUrl(sessionId);
        return ResponseEntity.ok(ApiResponse.success(widgetUrl));
    }
}


