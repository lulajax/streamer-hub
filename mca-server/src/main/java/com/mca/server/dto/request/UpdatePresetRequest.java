package com.mca.server.dto.request;

import com.mca.server.entity.Preset;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UpdatePresetRequest", description = "更新配置预设请求")
public class UpdatePresetRequest {

    @Schema(description = "预设名称", example = "我的PK预设")
    private String name;

    @Schema(description = "玩法模式", example = "PK")
    private Preset.GameMode gameMode;

    @Schema(description = "主播列表")
    private List<AnchorRequest> anchors;

    @Schema(description = "目标礼物列表")
    private List<TargetGiftRequest> targetGifts;

    @Schema(description = "玩法配置")
    private GameConfigRequest gameConfig;

    @Schema(description = "挂件显示设置")
    private WidgetSettingsRequest widgetSettings;
}
