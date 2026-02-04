package com.mca.server.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mca.server.entity.Preset;
import com.mca.server.entity.Session;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SessionDTO", description = "直播会话信息")
public class SessionDTO {
    
    @Schema(description = "会话ID", example = "sess_abc123")
    private String id;

    @Schema(description = "房间ID", example = "room_abc123")
    private String roomId;

    @Schema(description = "预设ID", example = "preset_xyz789")
    private String presetId;

    @Schema(description = "预设名称", example = "默认PK预设")
    private String presetName;

    @Schema(description = "玩法模式", example = "PK")
    private Preset.GameMode gameMode;

    @Schema(description = "会话状态", example = "RUNNING")
    private Session.SessionStatus status;

    @Schema(description = "当前轮次", example = "3")
    private Integer currentRound;

    @Schema(description = "总礼物数", example = "120")
    private Long totalGifts;

    @Schema(description = "总钻石数", example = "5600")
    private Long totalDiamonds;

    @Schema(description = "挂件访问令牌", example = "widget_token_abc123")
    private String widgetToken;

    @Schema(description = "挂件设置快照(JSON)", example = "{\"theme\":\"dark\"}")
    private String widgetSettingsSnapshot;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间", example = "2024-01-15 10:30:00")
    private LocalDateTime startedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间", example = "2024-01-15 11:00:00")
    private LocalDateTime endedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2024-01-15 10:00:00")
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
