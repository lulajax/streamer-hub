package com.mca.server.dto;

import com.mca.server.entity.Anchor;
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
@Schema(name = "AnchorDTO", description = "主播信息")
public class AnchorDTO {
    
    @Schema(description = "主播ID", example = "anchor_xyz789")
    private String id;

    @Schema(description = "TikTok ID", example = "tiktok_12345")
    private String tiktokId;

    @Schema(description = "主播名称", example = "主播小明")
    private String name;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "专属礼物ID列表", example = "[\"gift_1\",\"gift_2\"]")
    private List<String> exclusiveGifts;

    @Schema(description = "总积分", example = "1200")
    private Long totalScore;

    @Schema(description = "是否已淘汰", example = "false")
    private Boolean isEliminated;

    @Schema(description = "是否激活", example = "true")
    private Boolean isActive;

    @Schema(description = "展示顺序", example = "1")
    private Integer displayOrder;
    
    public static AnchorDTO fromEntity(Anchor entity) {
        return AnchorDTO.builder()
                .id(entity.getId())
                .tiktokId(entity.getTiktokId())
                .name(entity.getName())
                .avatarUrl(entity.getAvatarUrl())
                .exclusiveGifts(entity.getExclusiveGifts())
                .totalScore(entity.getTotalScore())
                .isEliminated(entity.getIsEliminated())
                .isActive(entity.getIsActive())
                .displayOrder(entity.getDisplayOrder())
                .build();
    }
}
