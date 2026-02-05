package com.mca.server.dto.request;

import com.mca.server.entity.Preset;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CreatePresetRequest", description = "创建配置预设请求")
public class CreatePresetRequest {

    @NotBlank(message = "配置名称不能为空")
    @Schema(description = "预设名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "默认PK预设")
    private String name;

    @NotNull(message = "玩法模式不能为空")
    @Schema(description = "玩法模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "PK")
    private Preset.GameMode gameMode;

    @Schema(description = "主播列表")
    private List<PresetAnchorRequest> anchors;

    @Schema(description = "目标礼物列表")
    private List<TargetGiftRequest> targetGifts;

    @Schema(description = "玩法配置")
    private GameConfigRequest gameConfig;

    @Schema(description = "挂件显示设置")
    private WidgetSettingsRequest widgetSettings;
}
