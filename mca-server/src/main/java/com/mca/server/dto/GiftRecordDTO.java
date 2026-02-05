package com.mca.server.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mca.server.entity.GiftRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "GiftRecordDTO", description = "礼物记录信息")
public class GiftRecordDTO {
    
    @Schema(description = "礼物记录ID", example = "gift_abc123")
    private String id;

    @Schema(description = "礼物ID", example = "gift_1001")
    private String giftId;

    @Schema(description = "礼物名称", example = "玫瑰")
    private String giftName;

    @Schema(description = "礼物图标URL", example = "https://example.com/gift.png")
    private String giftIcon;

    @Schema(description = "送礼用户ID", example = "user_abc123")
    private String userId;

    @Schema(description = "送礼用户名称", example = "粉丝小红")
    private String userName;

    @Schema(description = "送礼用户头像URL", example = "https://example.com/user.png")
    private String userAvatar;

    @Schema(description = "绑定主播ID", example = "anchor_xyz789")
    private String anchorId;

    @Schema(description = "绑定主播名称", example = "主播小明")
    private String anchorName;

    @Schema(description = "绑定主播头像URL", example = "https://example.com/anchor.png")
    private String anchorAvatar;

    @Schema(description = "送礼目标成员ID", example = "member_123")
    private String toMemberId;

    @Schema(description = "送礼目标成员昵称", example = "小星星")
    private String toMemberNickname;

    @Schema(description = "消息ID", example = "msg_001")
    private String messageId;

    @Schema(description = "消息时间戳(毫秒)", example = "1705305000000")
    private Long messageTimeStamp;

    @Schema(description = "礼物数量", example = "5")
    private Integer quantity;

    @Schema(description = "单个礼物钻石消耗", example = "20")
    private Integer diamondCost;

    @Schema(description = "总钻石消耗", example = "100")
    private Integer totalCost;

    @Schema(description = "绑定类型", example = "BOUND")
    private GiftRecord.BindType bindType;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2024-01-15 10:30:00")
    private LocalDateTime createdAt;
    
    public static GiftRecordDTO fromEntity(GiftRecord entity) {
        return GiftRecordDTO.builder()
                .id(entity.getId())
                .giftId(entity.getGiftId())
                .giftName(entity.getGiftName())
                .giftIcon(entity.getGiftIcon())
                .userId(entity.getUserId())
                .userName(entity.getUserName())
                .userAvatar(entity.getUserAvatar())
                .anchorId(entity.getAnchorId())
                .anchorName(entity.getAnchorName())
                .anchorAvatar(entity.getAnchorAvatar())
                .toMemberId(entity.getToMemberId())
                .toMemberNickname(entity.getToMemberNickname())
                .messageId(entity.getMessageId())
                .messageTimeStamp(entity.getMessageTimeStamp())
                .quantity(entity.getQuantity())
                .diamondCost(entity.getDiamondCost())
                .totalCost(entity.getTotalCost())
                .bindType(entity.getBindType())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
