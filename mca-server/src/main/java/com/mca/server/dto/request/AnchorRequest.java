package com.mca.server.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AnchorRequest", description = "主播信息请求")
public class AnchorRequest {

    @Schema(description = "主播ID（更新或绑定时可选）", example = "anchor_xyz789")
    private String id;

    @Schema(description = "TikTok ID", example = "tiktok_12345")
    private String tiktokId;

    @Schema(description = "主播名称", example = "主播小明")
    private String name;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "专属礼物ID列表", example = "[\"gift_1\",\"gift_2\"]")
    private List<String> exclusiveGifts;

    @Schema(description = "展示顺序", example = "1")
    private Integer displayOrder;
}
