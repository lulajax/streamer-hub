package com.mca.server.dto;

import com.mca.server.entity.Anchor;
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
@Schema(name = "UserAnchorDTO", description = "用户主播库信息")
public class UserAnchorDTO {

    @Schema(description = "主播ID", example = "anchor_xyz789")
    private String id;

    @Schema(description = "TikTok ID", example = "tiktok_12345")
    private String tiktokId;

    @Schema(description = "主播名称", example = "主播小明")
    private String name;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "创建时间", example = "2024-01-15 10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2024-01-20 12:00:00")
    private LocalDateTime updatedAt;

    public static UserAnchorDTO fromEntity(Anchor entity) {
        return UserAnchorDTO.builder()
                .id(entity.getId())
                .tiktokId(entity.getTiktokId())
                .name(entity.getName())
                .avatarUrl(entity.getAvatarUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
