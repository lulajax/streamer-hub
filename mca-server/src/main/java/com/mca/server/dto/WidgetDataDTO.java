package com.mca.server.dto;

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
@Schema(name = "WidgetDataDTO", description = "挂件展示数据")
public class WidgetDataDTO {
    @Schema(description = "挂件令牌", example = "widget_token_abc123")
    private String token;

    @Schema(description = "标题/名称", example = "主播PK榜单")
    private String name;

    @Schema(description = "玩法模式", example = "PK")
    private Preset.GameMode gameMode;

    @Schema(description = "状态", example = "RUNNING")
    private String status;

    @Schema(description = "当前轮次", example = "2")
    private Integer currentRound;

    @Schema(description = "礼物总数", example = "88")
    private Long totalGifts;

    @Schema(description = "钻石总数", example = "3200")
    private Long totalDiamonds;

    @Schema(description = "挂件设置(JSON)", example = "{\"theme\":\"dark\"}")
    private String widgetSettings;

    @Schema(description = "主播列表")
    private List<?> anchors;
}
