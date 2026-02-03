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
@Table(name = "sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preset_id")
    private Preset preset;

    @Column(name = "widget_token")
    private String widgetToken;

    @Column(name = "widget_settings_snapshot", columnDefinition = "TEXT")
    private String widgetSettingsSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_mode", nullable = false)
    private Preset.GameMode gameMode;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SessionStatus status = SessionStatus.IDLE;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    
    @Column(name = "paused_at")
    private LocalDateTime pausedAt;
    
    @Column(name = "current_round", nullable = false)
    @Builder.Default
    private Integer currentRound = 1;
    
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Round> rounds = new ArrayList<>();
    
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<GiftRecord> giftRecords = new ArrayList<>();
    
    @Column(name = "total_gifts", nullable = false)
    @Builder.Default
    private Long totalGifts = 0L;
    
    @Column(name = "total_diamonds", nullable = false)
    @Builder.Default
    private Long totalDiamonds = 0L;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum SessionStatus {
        IDLE,       // Not started
        RUNNING,    // Active
        PAUSED,     // Temporarily paused
        ENDED       // Completed
    }
    
    public void addGiftRecord(GiftRecord record) {
        giftRecords.add(record);
        totalGifts++;
        totalDiamonds += record.getTotalCost();
    }
}
