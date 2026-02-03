package com.mca.server.controller;

import com.mca.server.dto.ApiResponse;
import com.mca.server.dto.PresetDTO;
import com.mca.server.dto.request.*;
import com.mca.server.service.PresetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/presets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PresetController {

    private final PresetService presetService;

    @PostMapping
    public ResponseEntity<ApiResponse<PresetDTO>> createPreset(
            @RequestHeader("X-Device-Id") String deviceId,
            @RequestBody @Valid CreatePresetRequest request) {
        PresetDTO preset = presetService.createPreset(deviceId, request);
        return ResponseEntity.ok(ApiResponse.success("创建成功", preset));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PresetDTO>>> getDevicePresets(
            @RequestHeader("X-Device-Id") String deviceId) {
        List<PresetDTO> presets = presetService.getPresetsByDevice(deviceId);
        return ResponseEntity.ok(ApiResponse.success(presets));
    }

    @GetMapping("/{presetId}")
    public ResponseEntity<ApiResponse<PresetDTO>> getPreset(
            @PathVariable String presetId) {
        PresetDTO preset = presetService.getPreset(presetId);
        return ResponseEntity.ok(ApiResponse.success(preset));
    }

    @PutMapping("/{presetId}")
    public ResponseEntity<ApiResponse<PresetDTO>> updatePreset(
            @PathVariable String presetId,
            @RequestBody UpdatePresetRequest request) {
        PresetDTO preset = presetService.updatePreset(presetId, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", preset));
    }

    @DeleteMapping("/{presetId}")
    public ResponseEntity<ApiResponse<Void>> deletePreset(@PathVariable String presetId) {
        presetService.deletePreset(presetId);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @PutMapping("/{presetId}/game-config")
    public ResponseEntity<ApiResponse<PresetDTO>> updateGameConfig(
            @PathVariable String presetId,
            @RequestBody GameConfigRequest request) {
        PresetDTO preset = presetService.updateGameConfig(presetId, request);
        return ResponseEntity.ok(ApiResponse.success("玩法配置已更新", preset));
    }

    @PostMapping("/{presetId}/anchors")
    public ResponseEntity<ApiResponse<PresetDTO>> addAnchor(
            @PathVariable String presetId,
            @RequestBody @Valid AnchorRequest request) {
        PresetDTO preset = presetService.addAnchor(presetId, request);
        return ResponseEntity.ok(ApiResponse.success("主播已添加", preset));
    }

    @DeleteMapping("/{presetId}/anchors/{anchorId}")
    public ResponseEntity<ApiResponse<PresetDTO>> removeAnchor(
            @PathVariable String presetId,
            @PathVariable String anchorId) {
        PresetDTO preset = presetService.removeAnchor(presetId, anchorId);
        return ResponseEntity.ok(ApiResponse.success("主播已移除", preset));
    }

    @PutMapping("/{presetId}/anchors/{anchorId}/gifts")
    public ResponseEntity<ApiResponse<PresetDTO>> updateAnchorGifts(
            @PathVariable String presetId,
            @PathVariable String anchorId,
            @RequestBody List<String> giftIds) {
        PresetDTO preset = presetService.updateAnchorGifts(presetId, anchorId, giftIds);
        return ResponseEntity.ok(ApiResponse.success("专属礼物已更新", preset));
    }

    @PutMapping("/{presetId}/target-gifts")
    public ResponseEntity<ApiResponse<PresetDTO>> updateTargetGifts(
            @PathVariable String presetId,
            @RequestBody TargetGiftsRequest request) {
        PresetDTO preset = presetService.updateTargetGifts(presetId, request);
        return ResponseEntity.ok(ApiResponse.success("目标礼物已更新", preset));
    }

    @PutMapping("/{presetId}/widget-settings")
    public ResponseEntity<ApiResponse<PresetDTO>> updateWidgetSettings(
            @PathVariable String presetId,
            @RequestBody WidgetSettingsRequest request) {
        PresetDTO preset = presetService.updateWidgetSettings(presetId, request);
        return ResponseEntity.ok(ApiResponse.success("挂件样式已更新", preset));
    }

    @PostMapping("/{presetId}/widget-token/refresh")
    public ResponseEntity<ApiResponse<String>> refreshWidgetToken(
            @PathVariable String presetId) {
        String newToken = presetService.refreshWidgetToken(presetId);
        return ResponseEntity.ok(ApiResponse.success("挂件链接已更新", newToken));
    }

    @GetMapping("/{presetId}/preview-url")
    public ResponseEntity<ApiResponse<String>> getPreviewUrl(
            @PathVariable String presetId) {
        String previewUrl = presetService.generatePreviewUrl(presetId);
        return ResponseEntity.ok(ApiResponse.success(previewUrl));
    }

    @PostMapping("/{presetId}/set-default")
    public ResponseEntity<ApiResponse<PresetDTO>> setDefault(
            @PathVariable String presetId,
            @RequestHeader("X-Device-Id") String deviceId) {
        PresetDTO preset = presetService.setDefault(presetId, deviceId);
        return ResponseEntity.ok(ApiResponse.success("已设为默认配置", preset));
    }

    @GetMapping("/default")
    public ResponseEntity<ApiResponse<PresetDTO>> getDefaultPreset(
            @RequestHeader("X-Device-Id") String deviceId) {
        PresetDTO preset = presetService.getDefaultPreset(deviceId);
        return ResponseEntity.ok(ApiResponse.success(preset));
    }
}
