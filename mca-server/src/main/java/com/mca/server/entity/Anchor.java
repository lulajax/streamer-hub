package com.mca.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "anchors",
    indexes = {
        @Index(name = "idx_anchors_user_id", columnList = "user_id"),
        @Index(name = "idx_anchors_user_tiktok", columnList = "user_id, tiktok_id")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Anchor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "tiktok_id")
    private String tiktokId;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "avatar_url")
    private String avatarUrl;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
