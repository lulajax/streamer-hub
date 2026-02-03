package com.mca.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mca.server.dto.PresetDTO;
import com.mca.server.dto.request.*;
import com.mca.server.entity.Anchor;
import com.mca.server.entity.Preset;
import com.mca.server.exception.BusinessException;
import com.mca.server.repository.AnchorRepository;
import com.mca.server.repository.PresetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresetService {

    private final PresetRepository presetRepository;
    private final AnchorRepository anchorRepository;
    private final ObjectMapper objectMapper;

    private static final int MAX_PRESETS_PER_DEVICE = 10;

    @Transactional
    public PresetDTO createPreset(String deviceId, CreatePresetRequest request) {
        long count = presetRepository.countByDeviceId(deviceId);
        if (count >= MAX_PRESETS_PER_DEVICE) {
            throw new BusinessException("每个设备最多创建" + MAX_PRESETS_PER_DEVICE + "个配置");
        }

        Preset preset = Preset.builder()
                .name(request.getName())
                .gameMode(request.getGameMode())
                .deviceId(deviceId)
                .widgetToken(generateWidgetToken())
                .isDefault(count == 0)
                .anchors(new ArrayList<>())
                .build();

        if (request.getTargetGifts() != null) {
            preset.setTargetGiftsJson(toJson(request.getTargetGifts()));
        }

        if (request.getGameConfig() != null) {
            preset.setConfigJson(toJson(request.getGameConfig()));
        }

        if (request.getWidgetSettings() != null) {
            preset.setWidgetSettingsJson(toJson(request.getWidgetSettings()));
        }

        if (request.getAnchors() != null && !request.getAnchors().isEmpty()) {
            List<Anchor> anchors = request.getAnchors().stream()
                    .map(anchorReq -> Anchor.builder()
                            .tiktokId(anchorReq.getTiktokId())
                            .name(anchorReq.getName())
                            .avatarUrl(anchorReq.getAvatarUrl())
                            .exclusiveGifts(anchorReq.getExclusiveGifts() != null ? anchorReq.getExclusiveGifts() : new ArrayList<>())
                            .displayOrder(anchorReq.getDisplayOrder())
                            .totalScore(0L)
                            .isEliminated(false)
                            .isActive(true)
                            .preset(preset)
                            .build())
                    .collect(Collectors.toList());
            preset.getAnchors().addAll(anchors);
        }

        Preset saved = presetRepository.save(preset);
        log.info("Preset created: {} for device: {}", saved.getId(), deviceId);

        return PresetDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<PresetDTO> getPresetsByDevice(String deviceId) {
        return presetRepository.findByDeviceId(deviceId).stream()
                .map(PresetDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PresetDTO getPreset(String presetId) {
        Preset preset = presetRepository.findById(presetId)
                .orElseThrow(() -> new BusinessException("配置不存在"));
        return PresetDTO.fromEntity(preset);
    }

    @Transactional(readOnly = true)
    public PresetDTO getPresetByWidgetToken(String widgetToken) {
        Preset preset = presetRepository.findByWidgetToken(widgetToken)
                .orElseThrow(() -> new BusinessException("配置不存在"));
        return PresetDTO.fromEntity(preset);
    }

    @Transactional
    public PresetDTO updatePreset(String presetId, UpdatePresetRequest request) {
        Preset preset = presetRepository.findById(presetId)
                .orElseThrow(() -> new BusinessException("配置不存在"));

        if (request.getName() != null) {
            preset.setName(request.getName());
        }

        if (request.getGameMode() != null) {
            preset.setGameMode(request.getGameMode());
        }

        if (request.getTargetGifts() != null) {
            preset.setTargetGiftsJson(toJson(request.getTargetGifts()));
        }

        if (request.getGameConfig() != null) {
            preset.setConfigJson(toJson(request.getGameConfig()));
        }

        if (request.getWidgetSettings() != null) {
            preset.setWidgetSettingsJson(toJson(request.getWidgetSettings()));
        }

        Preset saved = presetRepository.save(preset);
        log.info("Preset updated: {}", presetId);

        return PresetDTO.fromEntity(saved);
    }

    @Transactional
    public void deletePreset(String presetId) {
        Preset preset = presetRepository.findById(presetId)
                .orElseThrow(() -> new BusinessException("配置不存在"));

        presetRepository.delete(preset);
        log.info("Preset deleted: {}", presetId);
    }

    @Transactional
    public PresetDTO updateGameConfig(String presetId, GameConfigRequest request) {
        Preset preset = presetRepository.findById(presetId)
                .orElseThrow(() -> new BusinessException("配置不存在"));

        preset.setConfigJson(toJson(request));
        Preset saved = presetRepository.save(preset);

        log.info("Game config updated for preset: {}", presetId);
        return PresetDTO.fromEntity(saved);
    }

    @Transactional
    public PresetDTO addAnchor(String presetId, AnchorRequest request) {
        Preset preset = presetRepository.findById(presetId)
                .orElseThrow(() -> new BusinessException("配置不存在"));

        Anchor anchor = Anchor.builder()
                .tiktokId(request.getTiktokId())
                .name(request.getName())
                .avatarUrl(request.getAvatarUrl())
                .exclusiveGifts(request.getExclusiveGifts() != null ? request.getExclusiveGifts() : new ArrayList<>())
                .displayOrder(request.getDisplayOrder())
                .totalScore(0L)
                .isEliminated(false)
                .isActive(true)
                .preset(preset)
                .build();

        preset.getAnchors().add(anchor);
        Preset saved = presetRepository.save(preset);

        log.info("Anchor added to preset: {}", presetId);
        return PresetDTO.fromEntity(saved);
    }

    @Transactional
    public PresetDTO removeAnchor(String presetId, String anchorId) {
        Preset preset = presetRepository.findById(presetId)
                .orElseThrow(() -> new BusinessException("配置不存在"));

        preset.getAnchors().removeIf(anchor -> anchor.getId().equals(anchorId));
        anchorRepository.deleteById(anchorId);

        Preset saved = presetRepository.save(preset);
        log.info("Anchor removed from preset: {}", presetId);
        return PresetDTO.fromEntity(saved);
    }

    @Transactional
    public PresetDTO updateAnchorGifts(String presetId, String anchorId, List<String> giftIds) {
        Preset preset = presetRepository.findById(presetId)
                .orElseThrow(() -> new BusinessException("配置不存在"));

        Anchor anchor = preset.getAnchors().stream()
                .filter(a -> a.getId().equals(anchorId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("主播不存在"));

        anchor.setExclusiveGifts(giftIds != null ? giftIds : new ArrayList<>());
        Preset saved = presetRepository.save(preset);

        log.info("Anchor gifts updated: {}", anchorId);
        return PresetDTO.fromEntity(saved);
    }

    @Transactional
    public PresetDTO updateTargetGifts(String presetId, TargetGiftsRequest request) {
        Preset preset = presetRepository.findById(presetId)
                .orElseThrow(() -> new BusinessException("配置不存在"));

        preset.setTargetGiftsJson(toJson(request));
        Preset saved = presetRepository.save(preset);

        log.info("Target gifts updated for preset: {}", presetId);
        return PresetDTO.fromEntity(saved);
    }

    @Transactional
    public PresetDTO updateWidgetSettings(String presetId, WidgetSettingsRequest request) {
        Preset preset = presetRepository.findById(presetId)
                .orElseThrow(() -> new BusinessException("配置不存在"));

        preset.setWidgetSettingsJson(toJson(request));
        Preset saved = presetRepository.save(preset);

        log.info("Widget settings updated for preset: {}", presetId);
        return PresetDTO.fromEntity(saved);
    }

    @Transactional
    public String refreshWidgetToken(String presetId) {
        Preset preset = presetRepository.findById(presetId)
                .orElseThrow(() -> new BusinessException("配置不存在"));

        String newToken = generateWidgetToken();
        preset.setWidgetToken(newToken);
        presetRepository.save(preset);

        log.info("Widget token refreshed for preset: {}", presetId);
        return newToken;
    }

    @Transactional(readOnly = true)
    public String generatePreviewUrl(String presetId) {
        Preset preset = presetRepository.findById(presetId)
                .orElseThrow(() -> new BusinessException("配置不存在"));

        return "/widget/" + preset.getWidgetToken() + "?mode=preset";
    }

    @Transactional(readOnly = true)
    public String generatePreviewUrlByToken(String widgetToken) {
        return "/widget/" + widgetToken + "?mode=preset";
    }

    @Transactional
    public PresetDTO setDefault(String presetId, String deviceId) {
        List<Preset> devicePresets = presetRepository.findByDeviceId(deviceId);

        for (Preset p : devicePresets) {
            p.setIsDefault(p.getId().equals(presetId));
        }

        presetRepository.saveAll(devicePresets);

        Preset preset = presetRepository.findById(presetId)
                .orElseThrow(() -> new BusinessException("配置不存在"));

        log.info("Default preset set: {} for device: {}", presetId, deviceId);
        return PresetDTO.fromEntity(preset);
    }

    @Transactional(readOnly = true)
    public PresetDTO getDefaultPreset(String deviceId) {
        return presetRepository.findByDeviceIdAndIsDefaultTrue(deviceId)
                .stream()
                .findFirst()
                .map(PresetDTO::fromEntity)
                .orElseThrow(() -> new BusinessException("没有默认配置"));
    }

    @Transactional(readOnly = true)
    public long countByDeviceId(String deviceId) {
        return presetRepository.countByDeviceId(deviceId);
    }

    private String generateWidgetToken() {
        return "wgt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new BusinessException("JSON序列化失败", e);
        }
    }
}
