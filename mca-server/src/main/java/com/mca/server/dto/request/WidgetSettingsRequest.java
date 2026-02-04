package com.mca.server.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "WidgetSettingsRequest", description = "挂件显示设置请求")
public class WidgetSettingsRequest {

    @Schema(description = "主题", example = "dark")
    private String theme;

    @Schema(description = "布局", example = "compact")
    private String layout;

    @Schema(description = "是否显示头像", example = "true")
    private Boolean showAvatar;

    @Schema(description = "是否显示进度条", example = "true")
    private Boolean showProgressBar;

    @Schema(description = "是否显示礼物动画", example = "true")
    private Boolean showGiftAnimation;

    @Schema(description = "背景颜色", example = "#000000")
    private String backgroundColor;

    @Schema(description = "文字颜色", example = "#FFFFFF")
    private String textColor;

    @Schema(description = "主色", example = "#FF3366")
    private String primaryColor;

    @Schema(description = "挂件位置")
    private WidgetPosition position;

    @Schema(description = "字体配置")
    private WidgetFonts fonts;

    @Schema(description = "动画配置")
    private WidgetAnimations animations;

    @Schema(description = "展示配置")
    private WidgetDisplay display;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "WidgetPosition", description = "挂件位置配置")
    public static class WidgetPosition {

        @Schema(description = "定位类型", example = "absolute")
        private String type;

        @Schema(description = "X 坐标", example = "100")
        private Integer x;

        @Schema(description = "Y 坐标", example = "200")
        private Integer y;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "WidgetFonts", description = "挂件字体配置")
    public static class WidgetFonts {

        @Schema(description = "标题字号", example = "24")
        private Integer titleSize;

        @Schema(description = "名称字号", example = "18")
        private Integer nameSize;

        @Schema(description = "分数字号", example = "20")
        private Integer scoreSize;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "WidgetAnimations", description = "挂件动画配置")
    public static class WidgetAnimations {

        @Schema(description = "排名变化动画", example = "slide")
        private String rankChange;

        @Schema(description = "新礼物动画", example = "pop")
        private String newGift;

        @Schema(description = "入场动画", example = "fade")
        private String entry;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "WidgetDisplay", description = "挂件展示配置")
    public static class WidgetDisplay {

        @Schema(description = "最大展示数量", example = "5")
        private Integer maxItems;

        @Schema(description = "是否展示自己排名", example = "true")
        private Boolean showSelfRank;

        @Schema(description = "是否高亮前三名", example = "true")
        private Boolean highlightTop3;
    }
}
