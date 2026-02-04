package com.mca.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "RegisterRequest", description = "用户注册请求")
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "用户邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "user@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(description = "用户密码，至少8位字符", requiredMode = Schema.RequiredMode.REQUIRED, example = "password123")
    private String password;

    @NotBlank(message = "Confirm password is required")
    @Schema(description = "确认密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "password123")
    private String confirmPassword;

    @Schema(description = "用户昵称", example = "主播小明")
    private String nickname;

    @Schema(description = "设备唯一标识", example = "device_abc123_xyz")
    private String deviceId;

    @Schema(description = "设备名称", example = "My PC")
    private String deviceName;
}
