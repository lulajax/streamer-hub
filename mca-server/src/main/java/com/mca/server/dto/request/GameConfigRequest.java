package com.mca.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "GameConfigRequest", description = "玩法配置请求")
public class GameConfigRequest {

    @Schema(description = "贴纸模式配置")
    private StickerModeConfig sticker;

    @Schema(description = "PK模式配置")
    private PKModeConfig pk;

    @Schema(description = "自由模式配置")
    private FreeModeConfig free;

    @Schema(description = "目标礼物列表")
    private List<TargetGiftRequest> targetGifts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "StickerModeConfig", description = "贴纸模式配置")
    public static class StickerModeConfig {

        @Schema(description = "模式类型", example = "normal")
        private String type;

        @Schema(description = "倒计时开关")
        private Boolean countdownEnabled;

        @Schema(description = "倒计时秒数", example = "60")
        private Integer countdownDuration;

        @Schema(description = "衰减开关")
        private Boolean decayEnabled;

        @Schema(description = "衰减秒数", example = "30")
        private Integer decayDuration;

        @Schema(description = "自动翻页开关")
        private Boolean autoFlipEnabled;

        @Schema(description = "翻页间隔", example = "10")
        private Integer flipInterval;

        @Schema(description = "最大页数", example = "2")
        private Integer maxPages;

        @Schema(description = "玩法礼物")
        private List<GameplayGift> gameplayGifts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "GameplayGift", description = "玩法礼物")
    public static class GameplayGift {

        @Schema(description = "礼物ID", example = "gift_1001")
        private String giftId;

        @Schema(description = "特效", example = "bounce")
        private String effect;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "PKModeConfig", description = "PK模式配置")
    public static class PKModeConfig {

        @Schema(description = "优势规则", example = "defender")
        private String advantage;

        @Schema(description = "计分方式", example = "individual")
        private String scoringMethod;

        @Schema(description = "倒计时开关")
        private Boolean countdownEnabled;

        @Schema(description = "倒计时秒数", example = "180")
        private Integer countdownDuration;

        @Schema(description = "单礼物绑定开关")
        private Boolean singleGiftBindEnabled;

        @Schema(description = "冻结特效开关")
        private Boolean freezeEffectEnabled;

        @Schema(description = "冻结阈值")
        private FreezeThresholds freezeThresholds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "FreezeThresholds", description = "冻结阈值")
    public static class FreezeThresholds {

        @Schema(description = "低档阈值", example = "20")
        private Integer low;

        @Schema(description = "中档阈值", example = "40")
        private Integer medium;

        @Schema(description = "高档阈值", example = "60")
        private Integer high;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "FreeModeConfig", description = "自由模式配置")
    public static class FreeModeConfig {

        @Schema(description = "回合时长(秒)", example = "300")
        private Integer roundDuration;

        @Schema(description = "目标积分", example = "10000")
        private Integer targetScore;

        @Schema(description = "是否显示回合数")
        private Boolean showRoundNumber;

        @Schema(description = "展示区间", example = "[1,10]")
        private List<Integer> displayRange;
    }
}
