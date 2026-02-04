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
@Schema(name = "GameConfigRequest", description = "玩法配置请求")
public class GameConfigRequest {

    @Schema(description = "贴纸舞玩法配置")
    private StickerDanceConfig stickerDance;

    @Schema(description = "PK玩法配置")
    private PKConfig pk;

    @Schema(description = "自由模式配置")
    private FreeModeConfig free;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "StickerDanceConfig", description = "贴纸舞玩法配置")
    public static class StickerDanceConfig {

        @Schema(description = "贴纸列表")
        private List<StickerConfig> stickers;

        @Schema(description = "切换模式", example = "SEQUENTIAL")
        private RotationMode rotationMode;

        @Schema(description = "切换间隔(秒)", example = "30")
        private Integer switchInterval;

        @Schema(description = "收到礼物自动切换", example = "true")
        private Boolean autoSwitchOnGift;

        public enum RotationMode {
            SEQUENTIAL,
            RANDOM
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "StickerConfig", description = "贴纸配置")
    public static class StickerConfig {

        @Schema(description = "贴纸ID", example = "sticker_01")
        private String id;

        @Schema(description = "贴纸名称", example = "星星贴纸")
        private String name;

        @Schema(description = "位置标识", example = "top-right")
        private String position;

        @Schema(description = "图片URL", example = "https://example.com/sticker.png")
        private String imageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "PKConfig", description = "PK玩法配置")
    public static class PKConfig {

        @Schema(description = "PK格式", example = "3v3")
        private String format;

        @Schema(description = "总轮数", example = "3")
        private Integer roundCount;

        @Schema(description = "每轮时长(秒)", example = "180")
        private Integer roundDuration;

        @Schema(description = "淘汰阈值", example = "100")
        private Integer eliminationThreshold;

        @Schema(description = "平局处理规则", example = "LAST_GIFT")
        private TieBreakerMode tieBreaker;

        public enum TieBreakerMode {
            LAST_GIFT,
            RANDOM,
            BOTH_WIN
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "FreeModeConfig", description = "自由模式配置")
    public static class FreeModeConfig {

        @Schema(description = "展示模式", example = "RANKING")
        private DisplayMode displayMode;

        @Schema(description = "是否显示礼物动画", example = "true")
        private Boolean showGiftAnimation;

        @Schema(description = "是否允许游客礼物", example = "false")
        private Boolean allowGuestGifts;

        public enum DisplayMode {
            RANKING,
            TIMELINE,
            NONE
        }
    }
}
