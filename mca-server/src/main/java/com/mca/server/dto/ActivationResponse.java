package com.mca.server.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
@Schema(name = "ActivationResponse", description = "设备激活结果")
public class ActivationResponse {
    
    @Schema(description = "是否已激活", example = "true")
    private boolean activated;

    @Schema(description = "设备名称", example = "My PC")
    private String deviceName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "激活时间", example = "2024-01-15 10:30:00")
    private LocalDateTime activatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "订阅过期时间", example = "2024-12-31 23:59:59")
    private LocalDateTime expiresAt;
    
    @Schema(description = "剩余有效天数", example = "30")
    private Long daysRemaining;
}
