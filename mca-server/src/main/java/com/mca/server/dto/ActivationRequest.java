package com.mca.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivationRequest {
    
    @NotBlank(message = "Activation code is required")
    @Size(min = 16, max = 19, message = "Activation code must be 16 characters")
    private String activationCode;
    
    @NotBlank(message = "Device ID is required")
    private String deviceId;
    
    private String deviceName;
}
