package com.mca.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Column(name = "nickname")
    private String nickname;
    
    @Column(name = "avatar_url")
    private String avatarUrl;
    
    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private Boolean isEmailVerified = false;
    
    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;
    
    @Column(name = "email_verification_token")
    private String emailVerificationToken;
    
    // Activation related fields
    @Column(name = "is_activated", nullable = false)
    @Builder.Default
    private Boolean isActivated = false;
    
    @Column(name = "activation_code")
    private String activationCode;
    
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;
    
    @Column(name = "activation_expires_at")
    private LocalDateTime activationExpiresAt;
    
    // Subscription/Premium status
    @Column(name = "subscription_type")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SubscriptionType subscriptionType = SubscriptionType.FREE;
    
    @Column(name = "subscription_expires_at")
    private LocalDateTime subscriptionExpiresAt;
    
    // Device tracking
    @Column(name = "device_id")
    private String deviceId;
    
    @Column(name = "device_name")
    private String deviceName;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "last_login_ip")
    private String lastLoginIp;
    
    // Account status
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "is_banned", nullable = false)
    @Builder.Default
    private Boolean isBanned = false;
    
    @Column(name = "ban_reason")
    private String banReason;
    
    // Password reset
    @Column(name = "password_reset_token")
    private String passwordResetToken;
    
    @Column(name = "password_reset_expires_at")
    private LocalDateTime passwordResetExpiresAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum SubscriptionType {
        FREE,       // 免费版
        BASIC,      // 基础版
        PRO,        // 专业版
        ENTERPRISE  // 企业版
    }
    
    public boolean hasActiveSubscription() {
        if (subscriptionType == SubscriptionType.FREE) {
            return false;
        }
        if (subscriptionExpiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(subscriptionExpiresAt);
    }
    
    public boolean isActivationValid() {
        if (!isActivated || activationExpiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(activationExpiresAt);
    }
}
