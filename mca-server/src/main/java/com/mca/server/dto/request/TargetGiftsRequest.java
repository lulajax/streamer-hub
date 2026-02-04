package com.mca.server.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "TargetGiftsRequest", description = "目标礼物与计分规则配置")
public class TargetGiftsRequest {

    @Schema(description = "目标礼物列表")
    private List<TargetGiftRequest> targetGifts;

    @Schema(description = "计分规则")
    private ScoringRulesRequest scoringRules;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "ScoringRulesRequest", description = "计分规则")
    public static class ScoringRulesRequest {

        @Schema(description = "计分模式", example = "DIAMOND")
        private ScoringMode mode;

        @Schema(description = "倍率", example = "1.5")
        private Double multiplier;

        public enum ScoringMode {
            DIAMOND,
            COUNT,
            POINTS
        }
    }
}
