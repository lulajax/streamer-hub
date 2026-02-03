package com.mca.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivatePremiumRequest {
    
    @NotBlank(message = "Activation code is required")
    private String activationCode;
    
    private String deviceId;
    
    private String deviceName;
}
