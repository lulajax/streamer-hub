package com.mca.server.dto;

import com.mca.server.entity.Preset;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresetDTO {
    
    private String id;
    private String name;
    private String deviceId;
    private Preset.GameMode gameMode;
    private List<AnchorDTO> anchors;
    private String targetGiftsJson;
    private String configJson;
    private String widgetSettingsJson;
    private String widgetToken;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PresetDTO fromEntity(Preset entity) {
        return PresetDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .deviceId(entity.getDeviceId())
                .gameMode(entity.getGameMode())
                .anchors(entity.getAnchors() != null ? entity.getAnchors().stream().map(AnchorDTO::fromEntity).toList() : null)
                .targetGiftsJson(entity.getTargetGiftsJson())
                .configJson(entity.getConfigJson())
                .widgetSettingsJson(entity.getWidgetSettingsJson())
                .widgetToken(entity.getWidgetToken())
                .isDefault(entity.getIsDefault())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
