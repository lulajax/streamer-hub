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
@Table(name = "activation_codes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivationCode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "code", nullable = false, unique = true)
    private String code;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type", nullable = false)
    private User.SubscriptionType subscriptionType;
    
    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private Boolean isUsed = false;
    
    @Column(name = "used_by_user_id")
    private String usedByUserId;
    
    @Column(name = "used_at")
    private LocalDateTime usedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
