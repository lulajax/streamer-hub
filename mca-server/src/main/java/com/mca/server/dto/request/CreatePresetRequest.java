package com.mca.server.dto.request;

import com.mca.server.entity.Preset;
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
public class CreatePresetRequest {

    @NotBlank(message = "配置名称不能为空")
    private String name;

    @NotNull(message = "玩法模式不能为空")
    private Preset.GameMode gameMode;

    private List<AnchorRequest> anchors;

    private List<TargetGiftRequest> targetGifts;

    private GameConfigRequest gameConfig;

    private WidgetSettingsRequest widgetSettings;
}
