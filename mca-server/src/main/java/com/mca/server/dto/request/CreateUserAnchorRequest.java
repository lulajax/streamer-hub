package com.mca.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CreateUserAnchorRequest", description = "创建用户主播请求")
public class CreateUserAnchorRequest {

    @Schema(description = "TikTok ID", example = "tiktok_12345")
    private String tiktokId;

    @NotBlank(message = "主播名称不能为空")
    @Schema(description = "主播名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "主播小明")
    private String name;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;
}
