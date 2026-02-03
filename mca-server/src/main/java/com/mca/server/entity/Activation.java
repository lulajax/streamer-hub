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
@Table(name = "activations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Activation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "activation_code", nullable = false, unique = true)
    private String activationCode;
    
    @Column(name = "device_id", nullable = false)
    private String deviceId;
    
    @Column(name = "device_name")
    private String deviceName;
    
    @Column(name = "is_activated", nullable = false)
    private Boolean isActivated;
    
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public boolean isValid() {
        if (!isActivated || expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(expiresAt);
    }
}
