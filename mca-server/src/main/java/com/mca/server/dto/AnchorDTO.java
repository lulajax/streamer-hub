package com.mca.server.dto;

import com.mca.server.entity.Anchor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnchorDTO {
    
    private String id;
    private String tiktokId;
    private String name;
    private String avatarUrl;
    private List<String> exclusiveGifts;
    private Long totalScore;
    private Boolean isEliminated;
    private Boolean isActive;
    private Integer displayOrder;
    
    public static AnchorDTO fromEntity(Anchor entity) {
        return AnchorDTO.builder()
                .id(entity.getId())
                .tiktokId(entity.getTiktokId())
                .name(entity.getName())
                .avatarUrl(entity.getAvatarUrl())
                .exclusiveGifts(entity.getExclusiveGifts())
                .totalScore(entity.getTotalScore())
                .isEliminated(entity.getIsEliminated())
                .isActive(entity.getIsActive())
                .displayOrder(entity.getDisplayOrder())
                .build();
    }
}
