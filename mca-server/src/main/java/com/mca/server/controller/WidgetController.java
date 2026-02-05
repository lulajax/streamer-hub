package com.mca.server.controller;

import com.mca.server.dto.ApiResponse;
import com.mca.server.dto.WidgetDataDTO;
import com.mca.server.service.PresetService;
import com.mca.server.service.SessionService;
import com.mca.server.service.WidgetDataService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/widget")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "挂件渲染", description = "OBS挂件渲染和数据获取接口（公开访问）")
public class WidgetController {

    private final PresetService presetService;
    private final SessionService sessionService;
    private final WidgetDataService widgetDataService;
    @Value("${mca.widget.base-url:http://localhost:3000}")
    private String widgetBaseUrl;

    @Operation(summary = "渲染挂件页面", description = "重定向到挂件渲染页面（用于OBS浏览器源嵌入）")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "302", description = "重定向到挂件页面"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "令牌无效或已过期"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @Hidden
    @GetMapping("/{token}")
    public ResponseEntity<Void> renderWidget(
            @Parameter(description = "挂件令牌", required = true, example = "widget_token_abc123") @PathVariable String token,
            @Parameter(description = "模式：preset-预设模式, session-会话模式", example = "preset") @RequestParam(defaultValue = "preset") String mode) {

        try {
            if ("session".equals(mode)) {
                sessionService.getSessionByWidgetToken(token);
            } else {
                presetService.getPresetByWidgetToken(token);
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }

        String apiBase = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();

        String redirectUrl = UriComponentsBuilder.fromUriString(widgetBaseUrl)
                .queryParam("token", token)
                .queryParam("mode", mode)
                .queryParam("apiBase", apiBase)
                .build()
                .toUriString();

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }

    @Operation(summary = "获取挂件数据", description = "获取挂件的实时数据（用于动态刷新）")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "令牌无效或已过期"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/{token}/data")
    public ResponseEntity<ApiResponse<WidgetDataDTO>> getWidgetData(
            @Parameter(description = "挂件令牌", required = true, example = "widget_token_abc123") @PathVariable String token,
            @Parameter(description = "模式：preset-预设模式, session-会话模式", example = "preset") @RequestParam(defaultValue = "preset") String mode) {

        WidgetDataDTO data = widgetDataService.getWidgetDataByToken(token, mode);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}


