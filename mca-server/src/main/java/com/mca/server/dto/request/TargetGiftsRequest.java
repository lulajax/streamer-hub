package com.mca.server.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetGiftsRequest {

    private List<TargetGiftRequest> targetGifts;

    private ScoringRulesRequest scoringRules;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoringRulesRequest {

        private ScoringMode mode;

        private Double multiplier;

        public enum ScoringMode {
            DIAMOND,
            COUNT,
            POINTS
        }
    }
}
