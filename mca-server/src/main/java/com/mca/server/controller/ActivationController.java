package com.mca.server.controller;

import com.mca.server.dto.ActivationRequest;
import com.mca.server.dto.ActivationResponse;
import com.mca.server.dto.ApiResponse;
import com.mca.server.service.ActivationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/activation")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ActivationController {
    
    private final ActivationService activationService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<ActivationResponse>> activate(
            @Valid @RequestBody ActivationRequest request) {
        return ResponseEntity.ok(activationService.activate(request));
    }
    
    @GetMapping("/check/{deviceId}")
    public ResponseEntity<ApiResponse<ActivationResponse>> checkActivation(
            @PathVariable String deviceId) {
        return ResponseEntity.ok(activationService.checkActivation(deviceId));
    }
    
    @PostMapping("/deactivate/{deviceId}")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String deviceId) {
        return ResponseEntity.ok(activationService.deactivate(deviceId));
    }
    
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<String>> generateCode() {
        String code = activationService.generateActivationCode();
        return ResponseEntity.ok(ApiResponse.success("Activation code generated", code));
    }
}
