package com.mca.server.service;

import com.mca.server.dto.*;
import com.mca.server.entity.ActivationCode;
import com.mca.server.entity.User;
import com.mca.server.entity.UserDevice;
import com.mca.server.repository.ActivationCodeRepository;
import com.mca.server.repository.UserDeviceRepository;
import com.mca.server.repository.UserRepository;
import com.mca.server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final ActivationCodeRepository activationCodeRepository;
    private final UserDeviceRepository userDeviceRepository;
    
    @Value("${mca.activation.default-expiry-days:365}")
    private int defaultActivationExpiryDays;

    @Value("${mca.activation.code-expiry-days:0}")
    private int activationCodeExpiryDays;

    @Value("${mca.auth.max-devices:1}")
    private int maxDevices;
    
    @Transactional
    public ApiResponse<UserDTO> register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        log.info("Processing registration for email: {}", normalizedEmail);
        
        // Validate password match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ApiResponse.error("Passwords do not match");
        }
        
        // Check if email already exists
        Optional<User> existingUserOpt = userRepository.findByEmail(normalizedEmail);
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            if (Boolean.TRUE.equals(existingUser.getIsEmailVerified())) {
                return ApiResponse.error("Email already registered");
            }
            if (Boolean.FALSE.equals(existingUser.getIsActive())) {
                return ApiResponse.error("Account is deactivated");
            }
            if (Boolean.TRUE.equals(existingUser.getIsBanned())) {
                return ApiResponse.error("Account has been banned. Reason: " + existingUser.getBanReason());
            }
            
            String verificationToken = UUID.randomUUID().toString();
            existingUser.setEmailVerificationToken(verificationToken);
            existingUser.setIsEmailVerified(false);
            existingUser.setEmailVerifiedAt(null);
            existingUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            existingUser.setNickname(request.getNickname() != null
                    ? request.getNickname()
                    : normalizedEmail.split("@")[0]);
            existingUser.setDeviceId(request.getDeviceId());
            existingUser.setDeviceName(request.getDeviceName());
            
            User savedUser = userRepository.save(existingUser);
            emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken);
            
            log.info("Verification email resent for unverified user: {}", savedUser.getEmail());
            
            return ApiResponse.success(
                    "Registration successful. Please check your email to verify your account.",
                    UserDTO.fromEntity(savedUser)
            );
        }
        
        // Generate email verification token
        String verificationToken = UUID.randomUUID().toString();
        
        // Create new user
        User user = User.builder()
                .email(normalizedEmail)
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

        LocalDateTime now = LocalDateTime.now();
        String deviceId = request.getDeviceId();
        String deviceName = request.getDeviceName();

        if (deviceId != null && !deviceId.isBlank()) {
            Optional<UserDevice> deviceOpt = userDeviceRepository.findByUserIdAndDeviceId(user.getId(), deviceId);
            if (deviceOpt.isPresent()) {
                UserDevice device = deviceOpt.get();
                if (deviceName != null) {
                    device.setDeviceName(deviceName);
                }
                device.setLastLoginAt(now);
                userDeviceRepository.save(device);
            } else {
                long deviceCount = userDeviceRepository.countByUserId(user.getId());
                if (deviceCount >= maxDevices) {
                    return ApiResponse.error("Device limit reached");
                }
                UserDevice newDevice = UserDevice.builder()
                        .userId(user.getId())
                        .deviceId(deviceId)
                        .deviceName(deviceName)
                        .firstSeenAt(now)
                        .lastLoginAt(now)
                        .build();
                userDeviceRepository.save(newDevice);
            }
        }
        
        // Update login info
        user.setLastLoginAt(now);
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
        
        String activationCodeValue = request.getActivationCode();
        // Validate activation code format
        if (!isValidActivationCode(activationCodeValue)) {
            return ApiResponse.error("Invalid activation code");
        }

        Optional<ActivationCode> codeOpt = activationCodeRepository.findByCode(activationCodeValue);
        if (codeOpt.isEmpty()) {
            return ApiResponse.error("Invalid activation code");
        }

        ActivationCode activationCode = codeOpt.get();
        if (Boolean.TRUE.equals(activationCode.getIsUsed())) {
            return ApiResponse.error("Activation code already used");
        }
        if (activationCode.isExpired()) {
            return ApiResponse.error("Activation code has expired");
        }

        User.SubscriptionType subscriptionType = activationCode.getSubscriptionType() != null
                ? activationCode.getSubscriptionType()
                : getSubscriptionTypeFromCode(activationCodeValue);
        
        // Activate user
        user.setIsActivated(true);
        user.setActivationCode(activationCodeValue);
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

        activationCode.setIsUsed(true);
        activationCode.setUsedAt(LocalDateTime.now());
        activationCode.setUsedByUserId(userId);
        activationCodeRepository.save(activationCode);
        
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
            return ApiResponse.success("If the email exists, a password reset link has been sent", null);
        }
        
        User user = userOpt.get();
        String resetToken = UUID.randomUUID().toString();
        
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusHours(24));
        userRepository.save(user);
        
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
        
        return ApiResponse.success("If the email exists, a password reset link has been sent", null);
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
        
        return ApiResponse.success("Password reset successful", null);
    }
    
    @Transactional
    public ApiResponse<String> generateActivationCode(String type) {
        User.SubscriptionType subscriptionType = getSubscriptionTypeFromType(type);
        String prefix = switch (subscriptionType) {
            case PRO -> "PRO";
            case ENTERPRISE -> "ENT";
            case BASIC, FREE -> "BAS";
        };

        String code;
        do {
            code = prefix + "-" + UUID.randomUUID().toString().replaceAll("-", "")
                    .substring(0, 12)
                    .toUpperCase();
        } while (activationCodeRepository.existsByCode(code));

        ActivationCode activationCode = ActivationCode.builder()
                .code(code)
                .subscriptionType(subscriptionType == User.SubscriptionType.FREE
                        ? User.SubscriptionType.BASIC
                        : subscriptionType)
                .expiresAt(activationCodeExpiryDays > 0
                        ? LocalDateTime.now().plusDays(activationCodeExpiryDays)
                        : null)
                .build();

        activationCodeRepository.save(activationCode);

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

    private User.SubscriptionType getSubscriptionTypeFromType(String type) {
        if (type == null) {
            return User.SubscriptionType.BASIC;
        }
        return switch (type.toUpperCase()) {
            case "PRO" -> User.SubscriptionType.PRO;
            case "ENTERPRISE", "ENT" -> User.SubscriptionType.ENTERPRISE;
            case "BASIC", "BAS" -> User.SubscriptionType.BASIC;
            default -> User.SubscriptionType.BASIC;
        };
    }
}
