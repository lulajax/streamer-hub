package com.mca.server.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mca.server.entity.GiftRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiftRecordDTO {
    
    private String id;
    private String giftId;
    private String giftName;
    private String giftIcon;
    private String userId;
    private String userName;
    private String userAvatar;
    private String anchorId;
    private String anchorName;
    private Integer quantity;
    private Integer diamondCost;
    private Integer totalCost;
    private GiftRecord.BindType bindType;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
                .quantity(entity.getQuantity())
                .diamondCost(entity.getDiamondCost())
                .totalCost(entity.getTotalCost())
                .bindType(entity.getBindType())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
