package com.mca.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "anchors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Anchor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "tiktok_id")
    private String tiktokId;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "avatar_url")
    private String avatarUrl;
    
    @ElementCollection
    @CollectionTable(name = "anchor_exclusive_gifts", joinColumns = @JoinColumn(name = "anchor_id"))
    @Column(name = "gift_id")
    @Builder.Default
    private List<String> exclusiveGifts = new ArrayList<>();
    
    @Column(name = "total_score", nullable = false)
    @Builder.Default
    private Long totalScore = 0L;
    
    @Column(name = "is_eliminated", nullable = false)
    @Builder.Default
    private Boolean isEliminated = false;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "display_order")
    private Integer displayOrder;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preset_id")
    private Preset preset;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
