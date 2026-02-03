package com.mca.server.controller;

import com.mca.server.dto.*;
import com.mca.server.service.AuthService;
import com.mca.server.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDTO>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        // Set device info from request headers if not provided
        if (request.getDeviceId() == null) {
            request.setDeviceId(httpRequest.getHeader("X-Device-Id"));
        }
        if (request.getDeviceName() == null) {
            request.setDeviceName(httpRequest.getHeader("X-Device-Name"));
        }
        return ResponseEntity.ok(authService.register(request));
    }
    
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<UserDTO>> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(authService.verifyEmail(token));
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDTO>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        // Set device info from request headers if not provided
        if (request.getDeviceId() == null) {
            request.setDeviceId(httpRequest.getHeader("X-Device-Id"));
        }
        if (request.getDeviceName() == null) {
            request.setDeviceName(httpRequest.getHeader("X-Device-Name"));
        }
        return ResponseEntity.ok(authService.login(request));
    }
    
    @PostMapping("/activate")
    public ResponseEntity<ApiResponse<UserDTO>> activatePremium(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ActivatePremiumRequest request) {
        String userId = extractUserIdFromToken(authHeader);
        return ResponseEntity.ok(authService.activatePremium(userId, request));
    }
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        String userId = extractUserIdFromToken(authHeader);
        return ResponseEntity.ok(authService.getCurrentUser(userId));
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
            @RequestParam String email) {
        return ResponseEntity.ok(authService.requestPasswordReset(email));
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        return ResponseEntity.ok(authService.resetPassword(token, newPassword));
    }
    
    @PostMapping("/generate-code")
    public ResponseEntity<ApiResponse<String>> generateActivationCode(
            @RequestParam(defaultValue = "BASIC") String type) {
        return ResponseEntity.ok(authService.generateActivationCode(type));
    }
    
    private String extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("Invalid or expired token");
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}
