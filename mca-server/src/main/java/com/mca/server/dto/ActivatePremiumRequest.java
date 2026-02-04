package com.mca.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 激活高级版请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ActivatePremiumRequest", description = "激活高级订阅请求")
public class ActivatePremiumRequest {

    @NotBlank(message = "Activation code is required")
    @Schema(description = "激活码", requiredMode = Schema.RequiredMode.REQUIRED, example = "ABCD-EFGH-IJKL-MNOP")
    private String activationCode;

    @Schema(description = "设备唯一标识", example = "device_abc123_xyz")
    private String deviceId;

    @Schema(description = "设备名称", example = "My PC")
    private String deviceName;
}
