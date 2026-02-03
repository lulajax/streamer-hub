package com.mca.server.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetGiftRequest {

    private String giftId;

    private String giftName;

    private String giftIcon;

    private Integer diamondCost;

    private Integer points;

    private Boolean isTarget;
}
