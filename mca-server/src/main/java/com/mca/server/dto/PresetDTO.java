package com.mca.server.dto;

import com.mca.server.entity.Preset;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PresetDTO", description = "直播配置预设")
public class PresetDTO {
    
    @Schema(description = "预设ID", example = "preset_abc123")
    private String id;

    @Schema(description = "预设名称", example = "默认PK预设")
    private String name;

    @Schema(description = "设备ID", example = "device_abc123_xyz")
    private String deviceId;

    @Schema(description = "玩法模式", example = "PK")
    private Preset.GameMode gameMode;

    @Schema(description = "主播列表")
    private List<AnchorDTO> anchors;

    @Schema(description = "目标礼物配置(JSON)", example = "[{\"giftId\":\"gift_1\",\"points\":10}]")
    private String targetGiftsJson;

    @Schema(description = "玩法配置(JSON)", example = "{\"pk\":{\"roundCount\":3}}")
    private String configJson;

    @Schema(description = "挂件设置(JSON)", example = "{\"theme\":\"dark\"}")
    private String widgetSettingsJson;

    @Schema(description = "挂件访问令牌", example = "widget_token_abc123")
    private String widgetToken;

    @Schema(description = "是否默认预设", example = "true")
    private Boolean isDefault;

    @Schema(description = "创建时间", example = "2024-01-15 10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2024-01-20 12:00:00")
    private LocalDateTime updatedAt;

    public static PresetDTO fromEntity(Preset entity) {
        return PresetDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .deviceId(entity.getDeviceId())
                .gameMode(entity.getGameMode())
                .anchors(entity.getAnchors() != null
                        ? entity.getAnchors().stream()
                        .sorted(Comparator.comparing(
                                presetAnchor -> presetAnchor.getDisplayOrder(),
                                Comparator.nullsLast(Integer::compareTo)
                        ))
                        .map(AnchorDTO::fromPresetAnchor)
                        .toList()
                        : null)
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
