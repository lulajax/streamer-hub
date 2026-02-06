package com.mca.server.controller;

import com.mca.server.dto.ApiResponse;
import com.mca.server.dto.PresetDTO;
import com.mca.server.dto.request.*;
import com.mca.server.service.PresetService;
import com.mca.server.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/presets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "配置预设", description = "直播配置预设的创建、管理和挂件设置接口")
@SecurityRequirement(name = "bearerAuth")
public class PresetController {

    private final PresetService presetService;

    @Operation(summary = "创建配置预设", description = "为指定设备创建新的直播配置预设")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "创建成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数验证失败"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该设备"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<PresetDTO>> createPreset(
            @Parameter(description = "设备ID", required = true, example = "device_abc123_xyz")
            @RequestHeader("X-Device-Id") String deviceId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "创建预设请求", required = true)
            @RequestBody @Valid CreatePresetRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        PresetDTO preset = presetService.createPreset(userId, deviceId, request);
        return ResponseEntity.ok(ApiResponse.success("创建成功", preset));
    }

    @Operation(summary = "获取设备预设列表", description = "获取指定设备的所有配置预设")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "设备ID无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该设备"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<PresetDTO>>> getDevicePresets(
            @Parameter(description = "设备ID", required = true, example = "device_abc123_xyz")
            @RequestHeader("X-Device-Id") String deviceId) {
        String userId = SecurityUtil.getCurrentUserId();
        List<PresetDTO> presets = presetService.getPresetsByDevice(userId, deviceId);
        return ResponseEntity.ok(ApiResponse.success(presets));
    }

    @Operation(summary = "获取预设详情", description = "获取指定配置预设的详细信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "预设ID无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该预设"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "预设不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/{presetId}")
    public ResponseEntity<ApiResponse<PresetDTO>> getPreset(
            @Parameter(description = "预设ID", required = true, example = "preset_abc123") @PathVariable String presetId) {
        String userId = SecurityUtil.getCurrentUserId();
        PresetDTO preset = presetService.getPreset(userId, presetId);
        return ResponseEntity.ok(ApiResponse.success(preset));
    }

    @Operation(summary = "更新配置预设", description = "更新指定配置预设的信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "更新成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数验证失败"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该预设"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "预设不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PutMapping("/{presetId}")
    public ResponseEntity<ApiResponse<PresetDTO>> updatePreset(
            @Parameter(description = "预设ID", required = true, example = "preset_abc123") @PathVariable String presetId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "更新预设请求", required = true)
            @RequestBody UpdatePresetRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        PresetDTO preset = presetService.updatePreset(userId, presetId, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", preset));
    }

    @Operation(summary = "删除配置预设", description = "删除指定的配置预设")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "删除成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "预设ID无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该预设"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "预设不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @DeleteMapping("/{presetId}")
    public ResponseEntity<ApiResponse<Void>> deletePreset(
            @Parameter(description = "预设ID", required = true, example = "preset_abc123") @PathVariable String presetId) {
        String userId = SecurityUtil.getCurrentUserId();
        presetService.deletePreset(userId, presetId);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @Operation(summary = "更新游戏配置", description = "更新指定预设的游戏玩法配置、目标礼物和计分规则")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "配置更新成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "配置参数无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该预设"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "预设不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PutMapping("/{presetId}/game-config")
    public ResponseEntity<ApiResponse<PresetDTO>> updateGameConfig(
            @Parameter(description = "预设ID", required = true, example = "preset_abc123") @PathVariable String presetId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "游戏玩法配置", required = true)
            @RequestBody GameConfigRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        PresetDTO preset = presetService.updateGameConfig(userId, presetId, request);
        return ResponseEntity.ok(ApiResponse.success("玩法配置已更新", preset));
    }

    @Operation(summary = "添加主播", description = "为配置预设添加新主播")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "主播添加成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数验证失败"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该预设"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "预设不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "主播已存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/{presetId}/anchors")
    public ResponseEntity<ApiResponse<PresetDTO>> addAnchor(
            @Parameter(description = "预设ID", required = true, example = "preset_abc123") @PathVariable String presetId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "主播信息", required = true)
            @RequestBody @Valid PresetAnchorRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        PresetDTO preset = presetService.addAnchor(userId, presetId, request);
        return ResponseEntity.ok(ApiResponse.success("主播已添加", preset));
    }

    @Operation(summary = "移除主播", description = "从配置预设中移除指定主播")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "主播移除成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该预设"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "预设或主播不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @DeleteMapping("/{presetId}/anchors/{anchorId}")
    public ResponseEntity<ApiResponse<PresetDTO>> removeAnchor(
            @Parameter(description = "预设ID", required = true, example = "preset_abc123") @PathVariable String presetId,
            @Parameter(description = "主播ID", required = true, example = "anchor_xyz789") @PathVariable String anchorId) {
        String userId = SecurityUtil.getCurrentUserId();
        PresetDTO preset = presetService.removeAnchor(userId, presetId, anchorId);
        return ResponseEntity.ok(ApiResponse.success("主播已移除", preset));
    }

    @Operation(summary = "更新主播专属礼物", description = "更新指定主播的专属礼物列表")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "礼物更新成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "礼物ID列表无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该预设"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "预设或主播不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PutMapping("/{presetId}/anchors/{anchorId}/gifts")
    public ResponseEntity<ApiResponse<PresetDTO>> updateAnchorGifts(
            @Parameter(description = "预设ID", required = true, example = "preset_abc123") @PathVariable String presetId,
            @Parameter(description = "主播ID", required = true, example = "anchor_xyz789") @PathVariable String anchorId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "专属礼物ID列表", required = true)
            @RequestBody List<String> giftIds) {
        String userId = SecurityUtil.getCurrentUserId();
        PresetDTO preset = presetService.updateAnchorGifts(userId, presetId, anchorId, giftIds);
        return ResponseEntity.ok(ApiResponse.success("专属礼物已更新", preset));
    }

    @Operation(summary = "更新挂件设置", description = "更新配置预设的挂件样式和显示设置")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "挂件设置更新成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "设置参数无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该预设"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "预设不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PutMapping("/{presetId}/widget-settings")
    public ResponseEntity<ApiResponse<PresetDTO>> updateWidgetSettings(
            @Parameter(description = "预设ID", required = true, example = "preset_abc123") @PathVariable String presetId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "挂件显示设置", required = true)
            @RequestBody WidgetSettingsRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        PresetDTO preset = presetService.updateWidgetSettings(userId, presetId, request);
        return ResponseEntity.ok(ApiResponse.success("挂件样式已更新", preset));
    }

    @Operation(summary = "刷新挂件令牌", description = "刷新配置预设的挂件访问令牌，使旧链接失效")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "令牌刷新成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "预设ID无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该预设"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "预设不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/{presetId}/widget-token/refresh")
    public ResponseEntity<ApiResponse<String>> refreshWidgetToken(
            @Parameter(description = "预设ID", required = true, example = "preset_abc123") @PathVariable String presetId) {
        String userId = SecurityUtil.getCurrentUserId();
        String newToken = presetService.refreshWidgetToken(userId, presetId);
        return ResponseEntity.ok(ApiResponse.success("挂件链接已更新", newToken));
    }

    @Operation(summary = "获取预览链接", description = "获取配置预设的挂件预览链接（用于OBS预览）")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "预设ID无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该预设"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "预设不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/{presetId}/preview-url")
    public ResponseEntity<ApiResponse<String>> getPreviewUrl(
            @Parameter(description = "预设ID", required = true, example = "preset_abc123") @PathVariable String presetId) {
        String userId = SecurityUtil.getCurrentUserId();
        String previewUrl = presetService.generatePreviewUrl(userId, presetId);
        return ResponseEntity.ok(ApiResponse.success(previewUrl));
    }
}


