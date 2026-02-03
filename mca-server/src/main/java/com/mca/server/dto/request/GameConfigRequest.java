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
public class GameConfigRequest {

    private StickerDanceConfig stickerDance;

    private PKConfig pk;

    private FreeModeConfig free;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StickerDanceConfig {

        private List<StickerConfig> stickers;

        private RotationMode rotationMode;

        private Integer switchInterval;

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
    public static class StickerConfig {

        private String id;

        private String name;

        private String position;

        private String imageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PKConfig {

        private String format;

        private Integer roundCount;

        private Integer roundDuration;

        private Integer eliminationThreshold;

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
    public static class FreeModeConfig {

        private DisplayMode displayMode;

        private Boolean showGiftAnimation;

        private Boolean allowGuestGifts;

        public enum DisplayMode {
            RANKING,
            TIMELINE,
            NONE
        }
    }
}
