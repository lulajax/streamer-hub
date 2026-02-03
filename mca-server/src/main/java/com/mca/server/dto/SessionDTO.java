package com.mca.server.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mca.server.entity.Preset;
import com.mca.server.entity.Session;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDTO {
    
    private String id;
    private String roomId;
    private String presetId;
    private String presetName;
    private Preset.GameMode gameMode;
    private Session.SessionStatus status;
    private Integer currentRound;
    private Long totalGifts;
    private Long totalDiamonds;
    private String widgetToken;
    private String widgetSettingsSnapshot;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    public static SessionDTO fromEntity(Session entity) {
        return SessionDTO.builder()
                .id(entity.getId())
                .roomId(entity.getRoom() != null ? entity.getRoom().getId() : null)
                .presetId(entity.getPreset() != null ? entity.getPreset().getId() : null)
                .presetName(entity.getPreset() != null ? entity.getPreset().getName() : null)
                .gameMode(entity.getGameMode())
                .status(entity.getStatus())
                .currentRound(entity.getCurrentRound())
                .totalGifts(entity.getTotalGifts())
                .totalDiamonds(entity.getTotalDiamonds())
                .widgetToken(entity.getWidgetToken())
                .widgetSettingsSnapshot(entity.getWidgetSettingsSnapshot())
                .startedAt(entity.getStartedAt())
                .endedAt(entity.getEndedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
