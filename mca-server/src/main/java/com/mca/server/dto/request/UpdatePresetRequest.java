package com.mca.server.dto.request;

import com.mca.server.entity.Preset;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePresetRequest {

    private String name;

    private Preset.GameMode gameMode;

    private List<AnchorRequest> anchors;

    private List<TargetGiftRequest> targetGifts;

    private GameConfigRequest gameConfig;

    private WidgetSettingsRequest widgetSettings;
}
