package com.mca.server.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
    name = "preset_anchors",
    uniqueConstraints = @UniqueConstraint(name = "uk_preset_anchor", columnNames = {"preset_id", "anchor_id"}),
    indexes = {
        @Index(name = "idx_preset_anchors_preset_id", columnList = "preset_id"),
        @Index(name = "idx_preset_anchors_anchor_id", columnList = "anchor_id")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresetAnchor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preset_id", nullable = false)
    private Preset preset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anchor_id", nullable = false)
    private Anchor anchor;

    @ElementCollection
    @CollectionTable(name = "preset_anchor_exclusive_gifts", joinColumns = @JoinColumn(name = "preset_anchor_id"))
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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
