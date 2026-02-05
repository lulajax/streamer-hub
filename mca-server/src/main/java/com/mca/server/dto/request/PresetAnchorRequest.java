package com.mca.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PresetAnchorRequest", description = "预设绑定主播请求")
public class PresetAnchorRequest {

    @NotBlank(message = "主播ID不能为空")
    @Schema(description = "主播ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "anchor_xyz789")
    private String anchorId;

    @Schema(description = "专属礼物ID列表", example = "[\"gift_1\",\"gift_2\"]")
    private List<String> exclusiveGifts;

    @Schema(description = "展示顺序", example = "1")
    private Integer displayOrder;
}
