package com.mca.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UpdateUserAnchorRequest", description = "更新用户主播请求")
public class UpdateUserAnchorRequest {

    @Schema(description = "TikTok ID", example = "tiktok_12345")
    private String tiktokId;

    @Schema(description = "主播名称", example = "主播小明")
    private String name;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;
}
