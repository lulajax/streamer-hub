package com.mca.server.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "TargetGiftRequest", description = "目标礼物配置")
public class TargetGiftRequest {

    @Schema(description = "礼物ID", example = "gift_1001")
    private String giftId;

    @Schema(description = "礼物名称", example = "玫瑰")
    private String giftName;

    @Schema(description = "礼物图标URL", example = "https://example.com/gift.png")
    private String giftIcon;

    @Schema(description = "钻石消耗", example = "20")
    private Integer diamondCost;

    @Schema(description = "积分值", example = "10")
    private Integer points;

    @Schema(description = "是否为目标礼物", example = "true")
    private Boolean isTarget;
}
