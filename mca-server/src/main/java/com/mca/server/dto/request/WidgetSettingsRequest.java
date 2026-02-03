package com.mca.server.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WidgetSettingsRequest {

    private String theme;

    private String layout;

    private Boolean showAvatar;

    private Boolean showProgressBar;

    private Boolean showGiftAnimation;

    private String backgroundColor;

    private String textColor;

    private String primaryColor;

    private WidgetPosition position;

    private WidgetFonts fonts;

    private WidgetAnimations animations;

    private WidgetDisplay display;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WidgetPosition {

        private String type;

        private Integer x;

        private Integer y;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WidgetFonts {

        private Integer titleSize;

        private Integer nameSize;

        private Integer scoreSize;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WidgetAnimations {

        private String rankChange;

        private String newGift;

        private String entry;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WidgetDisplay {

        private Integer maxItems;

        private Boolean showSelfRank;

        private Boolean highlightTop3;
    }
}
