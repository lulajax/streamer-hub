package com.mca.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备激活请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ActivationRequest", description = "设备激活请求（独立激活码方式）")
public class ActivationRequest {

    @NotBlank(message = "Activation code is required")
    @Size(min = 16, max = 19, message = "Activation code must be 16 characters")
    @Schema(description = "激活码", requiredMode = Schema.RequiredMode.REQUIRED, example = "ABCD-EFGH-IJKL-MNOP")
    private String activationCode;

    @NotBlank(message = "Device ID is required")
    @Schema(description = "设备唯一标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "device_abc123_xyz")
    private String deviceId;

    @Schema(description = "设备名称", example = "My PC")
    private String deviceName;
}
