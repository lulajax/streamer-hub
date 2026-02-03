package com.mca.server.dto;

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
public class WidgetDataDTO {
    private String token;
    private String name;
    private Preset.GameMode gameMode;
    private String status;
    private Integer currentRound;
    private Long totalGifts;
    private Long totalDiamonds;
    private String widgetSettings;
    private List<?> anchors;
}
