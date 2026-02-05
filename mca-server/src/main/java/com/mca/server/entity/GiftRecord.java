package com.mca.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "gift_records", indexes = {
    @Index(name = "idx_session_id", columnList = "session_id"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_anchor_id", columnList = "anchor_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiftRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "gift_id", nullable = false)
    private String giftId;
    
    @Column(name = "gift_name", nullable = false)
    private String giftName;
    
    @Column(name = "gift_icon")
    private String giftIcon;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "user_name", nullable = false)
    private String userName;
    
    @Column(name = "user_avatar")
    private String userAvatar;
    
    @Column(name = "anchor_id")
    private String anchorId;
    
    @Column(name = "anchor_name")
    private String anchorName;

    @Column(name = "anchor_avatar")
    private String anchorAvatar;

    @Column(name = "to_member_id")
    private String toMemberId;

    @Column(name = "to_member_nickname")
    private String toMemberNickname;

    @Column(name = "message_id")
    private String messageId;

    @Column(name = "message_timestamp")
    private Long messageTimeStamp;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "diamond_cost", nullable = false)
    private Integer diamondCost;
    
    @Column(name = "total_cost", nullable = false)
    private Integer totalCost;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "bind_type", nullable = false)
    private BindType bindType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    public enum BindType {
        AUTO,      // Auto-bound by exclusive gift
        MANUAL,    // Manually bound to anchor
        SINGLE,    // Single gift assignment
        NONE       // Not bound
    }
}
