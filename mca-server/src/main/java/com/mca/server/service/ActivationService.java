package com.mca.server.service;

import com.mca.server.dto.ActivationRequest;
import com.mca.server.dto.ActivationResponse;
import com.mca.server.dto.ApiResponse;
import com.mca.server.entity.Activation;
import com.mca.server.repository.ActivationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivationService {
    
    private final ActivationRepository activationRepository;
    
    @Value("${mca.activation.default-expiry-days:365}")
    private int defaultExpiryDays;
    
    @Transactional
    public ApiResponse<ActivationResponse> activate(ActivationRequest request) {
        log.info("Processing activation for device: {}", request.getDeviceId());
        
        // Check if already activated
        Optional<Activation> existingActivation = activationRepository.findByDeviceId(request.getDeviceId());
        if (existingActivation.isPresent() && existingActivation.get().isValid()) {
            return ApiResponse.error("Device already activated");
        }
        
        // Validate activation code (in production, this would check against a database of valid codes)
        if (!isValidActivationCode(request.getActivationCode())) {
            return ApiResponse.error("Invalid activation code");
        }
        
        // Create or update activation
        Activation activation = existingActivation.orElseGet(() -> Activation.builder()
                .deviceId(request.getDeviceId())
                .build());
        
        activation.setActivationCode(request.getActivationCode());
        activation.setDeviceName(request.getDeviceName());
        activation.setIsActivated(true);
        activation.setActivatedAt(LocalDateTime.now());
        activation.setExpiresAt(LocalDateTime.now().plusDays(defaultExpiryDays));
        activation.setLastUsedAt(LocalDateTime.now());
        
        activationRepository.save(activation);
        
        log.info("Activation successful for device: {}", request.getDeviceId());
        
        return ApiResponse.success("Activation successful", buildResponse(activation));
    }
    
    @Transactional(readOnly = true)
    public ApiResponse<ActivationResponse> checkActivation(String deviceId) {
        Optional<Activation> activationOpt = activationRepository.findByDeviceId(deviceId);
        
        if (activationOpt.isEmpty()) {
            return ApiResponse.success(ActivationResponse.builder()
                    .activated(false)
                    .build());
        }
        
        Activation activation = activationOpt.get();
        
        if (!activation.isValid()) {
            return ApiResponse.success(ActivationResponse.builder()
                    .activated(false)
                    .build());
        }
        
        // Update last used time
        activation.setLastUsedAt(LocalDateTime.now());
        activationRepository.save(activation);
        
        return ApiResponse.success(buildResponse(activation));
    }
    
    @Transactional
    public ApiResponse<Void> deactivate(String deviceId) {
        Optional<Activation> activationOpt = activationRepository.findByDeviceId(deviceId);
        
        if (activationOpt.isEmpty()) {
            return ApiResponse.error("Device not found");
        }
        
        Activation activation = activationOpt.get();
        activation.setIsActivated(false);
        activationRepository.save(activation);
        
        return ApiResponse.success("Device deactivated", null);
    }
    
    private boolean isValidActivationCode(String code) {
        // In production, validate against database of generated codes
        // For demo, accept any 16-character alphanumeric code
        String cleaned = code.replaceAll("-", "");
        return cleaned.matches("^[A-Z0-9]{16}$");
    }
    
    private ActivationResponse buildResponse(Activation activation) {
        long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), activation.getExpiresAt());
        
        return ActivationResponse.builder()
                .activated(activation.getIsActivated())
                .deviceName(activation.getDeviceName())
                .activatedAt(activation.getActivatedAt())
                .expiresAt(activation.getExpiresAt())
                .daysRemaining(Math.max(0, daysRemaining))
                .build();
    }
    
    @Transactional
    public String generateActivationCode() {
        // Generate a random 16-character code
        String code = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16).toUpperCase();
        String formatted = code.replaceAll("(.{4})", "$1-").substring(0, 19);
        return formatted;
    }
}
