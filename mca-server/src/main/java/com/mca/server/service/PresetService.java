package com.mca.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mca.server.dto.PresetDTO;
import com.mca.server.dto.request.*;
import com.mca.server.entity.Anchor;
import com.mca.server.entity.Preset;
import com.mca.server.entity.PresetAnchor;
import com.mca.server.exception.BusinessException;
import com.mca.server.mapper.PresetMapper;
import com.mca.server.repository.AnchorRepository;
import com.mca.server.repository.PresetAnchorRepository;
import com.mca.server.repository.PresetRepository;
import com.mca.server.repository.UserDeviceRepository;
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
    private final PresetAnchorRepository presetAnchorRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final ObjectMapper objectMapper;
    private final WidgetUpdatePublisher widgetUpdatePublisher;
    private final PresetMapper presetMapper;

    private static final int MAX_PRESETS_PER_DEVICE = 10;

    @Transactional
    public PresetDTO createPreset(String userId, String deviceId, CreatePresetRequest request) {
        validateDeviceOwnership(userId, deviceId);

        long count = presetRepository.countByDeviceIdAndUserId(deviceId, userId);
        if (count >= MAX_PRESETS_PER_DEVICE) {
            throw new BusinessException("每个设备最多创建" + MAX_PRESETS_PER_DEVICE + "个配置");
        }

        Preset preset = Preset.builder()
                .name(request.getName())
                .gameMode(request.getGameMode())
                .deviceId(deviceId)
                .userId(userId)
                .widgetToken(generateWidgetToken())
                .isDefault(count == 0)
                .anchors(new ArrayList<>())
                .build();

        if (request.getTargetGifts() != null) {
            preset.setTargetGiftsJson(buildTargetGiftsJson(request.getTargetGifts()));
        }

        if (request.getGameConfig() != null) {
            preset.setConfigJson(resolveGameConfigJson(request.getGameMode(), request.getGameConfig()));
        }

        if (request.getWidgetSettings() != null) {
            preset.setWidgetSettingsJson(toJson(request.getWidgetSettings()));
        }

        if (request.getAnchors() != null && !request.getAnchors().isEmpty()) {
            Set<String> seenAnchorIds = new HashSet<>();
            int nextOrder = 0;
            for (PresetAnchorRequest anchorReq : request.getAnchors()) {
                if (!seenAnchorIds.add(anchorReq.getAnchorId())) {
                    throw new BusinessException("主播重复绑定: " + anchorReq.getAnchorId());
                }
                Anchor anchor = getAnchorForUser(anchorReq.getAnchorId(), userId);
                Integer displayOrder = anchorReq.getDisplayOrder();
                if (displayOrder == null) {
                    displayOrder = nextOrder++;
                } else {
                    nextOrder = Math.max(nextOrder, displayOrder + 1);
                }
                preset.getAnchors().add(buildPresetAnchor(preset, anchor, anchorReq, displayOrder));
            }
        }

        Preset saved = presetRepository.save(preset);
        log.info("Preset created: {} for device: {}", saved.getId(), deviceId);

        return presetMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<PresetDTO> getPresetsByDevice(String userId, String deviceId) {
        validateDeviceOwnership(userId, deviceId);
        List<Preset> presets = presetRepository.findWithAnchorsByDeviceIdAndUserId(deviceId, userId);
        return presetMapper.toDtoList(presets);
    }

    @Transactional(readOnly = true)
    public PresetDTO getPreset(String userId, String presetId) {
        Preset preset = presetRepository.findWithAnchorsById(presetId)
                .orElseThrow(() -> new BusinessException("配置不存在"));
        if (!userId.equals(preset.getUserId())) {
            throw new BusinessException("无权限访问该预设");
        }
        return presetMapper.toDto(preset);
    }

    @Transactional(readOnly = true)
    public PresetDTO getPreset(String presetId) {
        Preset preset = presetRepository.findWithAnchorsById(presetId)
                .orElseThrow(() -> new BusinessException("配置不存在"));
        return presetMapper.toDto(preset);
    }

    @Transactional(readOnly = true)
    public PresetDTO getPresetByWidgetToken(String widgetToken) {
        Preset preset = presetRepository.findWithAnchorsByWidgetToken(widgetToken)
                .orElseThrow(() -> new BusinessException("配置不存在"));
        return presetMapper.toDto(preset);
    }

    @Transactional
    public PresetDTO updatePreset(String userId, String presetId, UpdatePresetRequest request) {
        Preset preset = getPresetForUser(presetId, userId);

        if (request.getName() != null) {
            preset.setName(request.getName());
        }

        if (request.getGameMode() != null) {
            preset.setGameMode(request.getGameMode());
        }

        if (request.getTargetGifts() != null) {
            preset.setTargetGiftsJson(buildTargetGiftsJson(request.getTargetGifts()));
        }

        if (request.getGameConfig() != null) {
            preset.setConfigJson(resolveGameConfigJson(preset.getGameMode(), request.getGameConfig()));
        }

        if (request.getWidgetSettings() != null) {
            preset.setWidgetSettingsJson(toJson(request.getWidgetSettings()));
        }

        Preset saved = presetRepository.save(preset);
        log.info("Preset updated: {}", presetId);
        widgetUpdatePublisher.publishForPreset(presetId);

        return presetMapper.toDto(saved);
    }

    @Transactional
    public void deletePreset(String userId, String presetId) {
        Preset preset = getPresetForUser(presetId, userId);

        presetRepository.delete(preset);
        log.info("Preset deleted: {}", presetId);
    }

    @Transactional
    public PresetDTO updateGameConfig(String userId, String presetId, GameConfigRequest request) {
        Preset preset = getPresetForUser(presetId, userId);

        // 更新游戏配置
        String configJson = resolveGameConfigJson(preset.getGameMode(), request);
        if (configJson != null) {
            preset.setConfigJson(configJson);
        }

        // 更新目标礼物（如果提供）
        if (request.getTargetGifts() != null) {
            preset.setTargetGiftsJson(buildTargetGiftsJson(request.getTargetGifts()));
        }

        Preset saved = presetRepository.save(preset);

        log.info("Game config updated for preset: {}", presetId);
        widgetUpdatePublisher.publishForPreset(presetId);
        return presetMapper.toDto(saved);
    }

    @Transactional
    public PresetDTO addAnchor(String userId, String presetId, PresetAnchorRequest request) {
        Preset preset = getPresetForUser(presetId, userId);
        Anchor anchor = getAnchorForUser(request.getAnchorId(), userId);

        if (presetAnchorRepository.findByPresetIdAndAnchorId(presetId, anchor.getId()).isPresent()) {
            throw new BusinessException("主播已存在");
        }

        Integer displayOrder = request.getDisplayOrder();
        if (displayOrder == null) {
            displayOrder = getNextDisplayOrder(preset);
        }

        PresetAnchor presetAnchor = buildPresetAnchor(preset, anchor, request, displayOrder);
        preset.getAnchors().add(presetAnchor);

        Preset saved = presetRepository.save(preset);

        log.info("Anchor added to preset: {}", presetId);
        widgetUpdatePublisher.publishForPreset(presetId);
        return presetMapper.toDto(saved);
    }

    @Transactional
    public PresetDTO removeAnchor(String userId, String presetId, String anchorId) {
        Preset preset = getPresetForUser(presetId, userId);

        PresetAnchor presetAnchor = presetAnchorRepository.findByPresetIdAndAnchorId(presetId, anchorId)
                .orElseThrow(() -> new BusinessException("主播不存在"));

        preset.getAnchors().removeIf(item -> item.getAnchor().getId().equals(anchorId));
        presetAnchorRepository.delete(presetAnchor);

        Preset saved = presetRepository.save(preset);
        log.info("Anchor removed from preset: {}", presetId);
        widgetUpdatePublisher.publishForPreset(presetId);
        return presetMapper.toDto(saved);
    }

    @Transactional
    public PresetDTO updateAnchorGifts(String userId, String presetId, String anchorId, List<String> giftIds) {
        Preset preset = getPresetForUser(presetId, userId);

        PresetAnchor presetAnchor = preset.getAnchors().stream()
                .filter(item -> item.getAnchor().getId().equals(anchorId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("主播不存在"));

        presetAnchor.setExclusiveGifts(giftIds != null ? giftIds : new ArrayList<>());
        Preset saved = presetRepository.save(preset);

        log.info("Anchor gifts updated: {}", anchorId);
        widgetUpdatePublisher.publishForPreset(presetId);
        return presetMapper.toDto(saved);
    }

    @Transactional
    public PresetDTO updateWidgetSettings(String userId, String presetId, WidgetSettingsRequest request) {
        Preset preset = getPresetForUser(presetId, userId);

        preset.setWidgetSettingsJson(toJson(request));
        Preset saved = presetRepository.save(preset);

        log.info("Widget settings updated for preset: {}", presetId);
        widgetUpdatePublisher.publishForPreset(presetId);
        return presetMapper.toDto(saved);
    }

    @Transactional
    public String refreshWidgetToken(String userId, String presetId) {
        Preset preset = getPresetForUser(presetId, userId);

        String newToken = generateWidgetToken();
        preset.setWidgetToken(newToken);
        presetRepository.save(preset);

        log.info("Widget token refreshed for preset: {}", presetId);
        return newToken;
    }

    @Transactional(readOnly = true)
    public String generatePreviewUrl(String userId, String presetId) {
        Preset preset = getPresetForUser(presetId, userId);

        return "/widget/" + preset.getWidgetToken() + "?mode=preset";
    }

    @Transactional(readOnly = true)
    public String generatePreviewUrlByToken(String widgetToken) {
        return "/widget/" + widgetToken + "?mode=preset";
    }

    @Transactional(readOnly = true)
    public long countByDeviceId(String userId, String deviceId) {
        validateDeviceOwnership(userId, deviceId);
        return presetRepository.countByDeviceIdAndUserId(deviceId, userId);
    }

    private void validateDeviceOwnership(String userId, String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            throw new BusinessException("设备ID不能为空");
        }
        if (userId == null || userId.isBlank()) {
            throw new BusinessException("用户未认证");
        }
        if (userDeviceRepository.findByUserIdAndDeviceId(userId, deviceId).isEmpty()) {
            throw new BusinessException("无权限访问该设备");
        }
    }

    private Preset getPresetForUser(String presetId, String userId) {
        Preset preset = presetRepository.findById(presetId)
                .orElseThrow(() -> new BusinessException("配置不存在"));
        if (!userId.equals(preset.getUserId())) {
            throw new BusinessException("无权限访问该预设");
        }
        return preset;
    }

    private Anchor getAnchorForUser(String anchorId, String userId) {
        Anchor anchor = anchorRepository.findById(anchorId)
                .orElseThrow(() -> new BusinessException("主播不存在"));
        if (!userId.equals(anchor.getUserId())) {
            throw new BusinessException("无权限访问该主播");
        }
        return anchor;
    }

    private PresetAnchor buildPresetAnchor(Preset preset, Anchor anchor, PresetAnchorRequest request, Integer displayOrder) {
        return PresetAnchor.builder()
                .preset(preset)
                .anchor(anchor)
                .exclusiveGifts(request.getExclusiveGifts() != null ? request.getExclusiveGifts() : new ArrayList<>())
                .displayOrder(displayOrder)
                .totalScore(0L)
                .isEliminated(false)
                .isActive(true)
                .build();
    }

    private int getNextDisplayOrder(Preset preset) {
        return preset.getAnchors().stream()
                .map(PresetAnchor::getDisplayOrder)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .map(value -> value + 1)
                .orElse(0);
    }

    private String generateWidgetToken() {
        return "wgt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String resolveGameConfigJson(Preset.GameMode mode, GameConfigRequest request) {
        if (request == null || mode == null) {
            return null;
        }
        Object payload = switch (mode) {
            case STICKER -> request.getSticker();
            case PK -> request.getPk();
            case FREE -> request.getFree();
        };
        if (payload == null) {
            return null;
        }
        return toJson(payload);
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new BusinessException("JSON序列化失败", e);
        }
    }

    private String buildTargetGiftsJson(List<TargetGiftRequest> targetGifts) {
        return toJson(Map.of(
                "targetGifts", targetGifts != null ? targetGifts : List.of()
        ));
    }
}
