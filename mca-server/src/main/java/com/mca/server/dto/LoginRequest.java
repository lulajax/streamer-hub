package com.mca.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "LoginRequest", description = "用户登录请求")
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "用户邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "user@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "用户密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "password123")
    private String password;

    @Schema(description = "设备唯一标识", example = "device_abc123_xyz")
    private String deviceId;

    @Schema(description = "设备名称", example = "My PC")
    private String deviceName;
}
