package com.mca.server.service;

import com.mca.server.dto.*;
import com.mca.server.entity.User;
import com.mca.server.repository.UserRepository;
import com.mca.server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    
    @Value("${mca.activation.default-expiry-days:365}")
    private int defaultActivationExpiryDays;
    
    @Transactional
    public ApiResponse<UserDTO> register(RegisterRequest request) {
        log.info("Processing registration for email: {}", request.getEmail());
        
        // Validate password match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ApiResponse.error("Passwords do not match");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.error("Email already registered");
        }
        
        // Generate email verification token
        String verificationToken = UUID.randomUUID().toString();
        
        // Create new user
        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname() != null ? request.getNickname() : request.getEmail().split("@")[0])
                .deviceId(request.getDeviceId())
                .deviceName(request.getDeviceName())
                .isEmailVerified(false)
                .emailVerificationToken(verificationToken)
                .isActivated(false)
                .subscriptionType(User.SubscriptionType.FREE)
                .isActive(true)
                .isBanned(false)
                .build();
        
        User savedUser = userRepository.save(user);
        
        // Send verification email
        emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken);
        
        log.info("User registered successfully: {}", savedUser.getEmail());
        
        return ApiResponse.success("Registration successful. Please check your email to verify your account.", 
                UserDTO.fromEntity(savedUser));
    }
    
    @Transactional
    public ApiResponse<UserDTO> verifyEmail(String token) {
        log.info("Processing email verification");
        
        Optional<User> userOpt = userRepository.findByEmailVerificationToken(token);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("Invalid verification token");
        }
        
        User user = userOpt.get();
        user.setIsEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setEmailVerificationToken(null);
        
        userRepository.save(user);
        
        log.info("Email verified successfully for user: {}", user.getEmail());
        
        // Generate tokens for auto-login
        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        
        return ApiResponse.success("Email verified successfully", 
                UserDTO.fromEntityWithTokens(user, accessToken, refreshToken));
    }
    
    @Transactional
    public ApiResponse<UserDTO> login(LoginRequest request) {
        log.info("Processing login for email: {}", request.getEmail());
        
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail().toLowerCase().trim());
        if (userOpt.isEmpty()) {
            return ApiResponse.error("Invalid email or password");
        }
        
        User user = userOpt.get();
        
        // Check if account is active
        if (!user.getIsActive()) {
            return ApiResponse.error("Account is deactivated");
        }
        
        // Check if account is banned
        if (user.getIsBanned()) {
            return ApiResponse.error("Account has been banned. Reason: " + user.getBanReason());
        }
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ApiResponse.error("Invalid email or password");
        }
        
        // Check if email is verified
        if (!user.getIsEmailVerified()) {
            return ApiResponse.error("Please verify your email before logging in");
        }
        
        // Update login info
        user.setLastLoginAt(LocalDateTime.now());
        if (request.getDeviceId() != null) {
            user.setDeviceId(request.getDeviceId());
        }
        if (request.getDeviceName() != null) {
            user.setDeviceName(request.getDeviceName());
        }
        
        userRepository.save(user);
        
        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        
        log.info("User logged in successfully: {}", user.getEmail());
        
        return ApiResponse.success("Login successful", 
                UserDTO.fromEntityWithTokens(user, accessToken, refreshToken));
    }
    
    @Transactional
    public ApiResponse<UserDTO> activatePremium(String userId, ActivatePremiumRequest request) {
        log.info("Processing premium activation for user: {}", userId);
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("User not found");
        }
        
        User user = userOpt.get();
        
        // Validate activation code
        if (!isValidActivationCode(request.getActivationCode())) {
            return ApiResponse.error("Invalid activation code");
        }
        
        // Check if code is already used
        Optional<User> existingUser = userRepository.findByActivationCode(request.getActivationCode());
        if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
            return ApiResponse.error("Activation code already used");
        }
        
        // Determine subscription type based on code prefix
        User.SubscriptionType subscriptionType = getSubscriptionTypeFromCode(request.getActivationCode());
        
        // Activate user
        user.setIsActivated(true);
        user.setActivationCode(request.getActivationCode());
        user.setActivatedAt(LocalDateTime.now());
        user.setActivationExpiresAt(LocalDateTime.now().plusDays(defaultActivationExpiryDays));
        user.setSubscriptionType(subscriptionType);
        user.setSubscriptionExpiresAt(LocalDateTime.now().plusDays(defaultActivationExpiryDays));
        
        if (request.getDeviceId() != null) {
            user.setDeviceId(request.getDeviceId());
        }
        if (request.getDeviceName() != null) {
            user.setDeviceName(request.getDeviceName());
        }
        
        User savedUser = userRepository.save(user);
        
        log.info("Premium activated successfully for user: {}, type: {}", 
                user.getEmail(), subscriptionType);
        
        return ApiResponse.success("Premium activated successfully", UserDTO.fromEntity(savedUser));
    }
    
    @Transactional(readOnly = true)
    public ApiResponse<UserDTO> getCurrentUser(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("User not found");
        }
        
        return ApiResponse.success(UserDTO.fromEntity(userOpt.get()));
    }
    
    @Transactional
    public ApiResponse<Void> requestPasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email.toLowerCase().trim());
        if (userOpt.isEmpty()) {
            // Don't reveal if email exists
            return ApiResponse.success("If the email exists, a password reset link has been sent");
        }
        
        User user = userOpt.get();
        String resetToken = UUID.randomUUID().toString();
        
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusHours(24));
        userRepository.save(user);
        
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
        
        return ApiResponse.success("If the email exists, a password reset link has been sent");
    }
    
    @Transactional
    public ApiResponse<Void> resetPassword(String token, String newPassword) {
        Optional<User> userOpt = userRepository.findByPasswordResetToken(token);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("Invalid reset token");
        }
        
        User user = userOpt.get();
        
        if (user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
            return ApiResponse.error("Reset token has expired");
        }
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);
        userRepository.save(user);
        
        return ApiResponse.success("Password reset successful");
    }
    
    @Transactional
    public ApiResponse<String> generateActivationCode(String type) {
        // Generate activation code with prefix indicating subscription type
        String prefix = switch (type.toUpperCase()) {
            case "BASIC" -> "BAS";
            case "PRO" -> "PRO";
            case "ENTERPRISE" -> "ENT";
            default -> "BAS";
        };
        
        String code = prefix + "-" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12).toUpperCase();
        
        return ApiResponse.success("Activation code generated", code);
    }
    
    private boolean isValidActivationCode(String code) {
        // Check format: TYPE-XXXXXXXXXXXX (e.g., BAS-ABC123DEF456)
        return code != null && code.matches("^(BAS|PRO|ENT)-[A-Z0-9]{12}$");
    }
    
    private User.SubscriptionType getSubscriptionTypeFromCode(String code) {
        if (code.startsWith("PRO")) {
            return User.SubscriptionType.PRO;
        } else if (code.startsWith("ENT")) {
            return User.SubscriptionType.ENTERPRISE;
        } else {
            return User.SubscriptionType.BASIC;
        }
    }
}
