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
@Table(name = "presets", indexes = {
    @Index(name = "idx_device_id", columnList = "device_id"),
    @Index(name = "idx_widget_token", columnList = "widget_token", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Preset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_mode", nullable = false)
    private GameMode gameMode;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @OneToMany(mappedBy = "preset", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Anchor> anchors = new ArrayList<>();

    @Column(name = "target_gifts_json", columnDefinition = "TEXT")
    private String targetGiftsJson;

    @Column(name = "config_json", columnDefinition = "TEXT")
    private String configJson;

    @Column(name = "widget_settings_json", columnDefinition = "TEXT")
    private String widgetSettingsJson;

    @Column(name = "widget_token", unique = true)
    private String widgetToken;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum GameMode {
        STICKER,    // 贴纸舞模式
        PK,         // 攻守擂模式
        FREE        // 自由模式
    }
}
