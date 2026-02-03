package com.mca.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "rounds")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Round {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;
    
    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;
    
    @Column(name = "anchor_id")
    private String anchorId;
    
    @Column(name = "anchor_name")
    private String anchorName;
    
    @Column(name = "score", nullable = false)
    @Builder.Default
    private Long score = 0L;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "winner_id")
    private String winnerId;
    
    @Column(name = "is_target_reached")
    private Boolean isTargetReached;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
