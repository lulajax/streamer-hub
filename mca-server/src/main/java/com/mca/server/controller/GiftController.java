package com.mca.server.controller;

import com.mca.server.dto.ApiResponse;
import com.mca.server.dto.GiftRecordDTO;
import com.mca.server.service.GiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gifts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "礼物管理", description = "直播礼物记录、绑定和统计接口")
@SecurityRequirement(name = "bearerAuth")
public class GiftController {
    
    private final GiftService giftService;
    
    @Operation(summary = "记录礼物", description = "记录一个新的礼物到指定会话")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "礼物记录成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误或会话不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该会话"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/session/{sessionId}")
    public ResponseEntity<ApiResponse<GiftRecordDTO>> recordGift(
            @Parameter(description = "会话ID", required = true, example = "sess_abc123") @PathVariable String sessionId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "礼物记录信息", required = true)
            @RequestBody GiftRecordDTO giftDTO) {
        GiftRecordDTO recorded = giftService.recordGift(sessionId, giftDTO);
        return ResponseEntity.ok(ApiResponse.success("Gift recorded", recorded));
    }
    
    @Operation(summary = "获取会话礼物列表", description = "获取指定会话的所有礼物记录")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "会话ID无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该会话"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ApiResponse<List<GiftRecordDTO>>> getSessionGifts(
            @Parameter(description = "会话ID", required = true, example = "sess_abc123") @PathVariable String sessionId) {
        List<GiftRecordDTO> gifts = giftService.getSessionGifts(sessionId);
        return ResponseEntity.ok(ApiResponse.success(gifts));
    }

    @Operation(summary = "获取礼物列表", description = "获取指定会话的所有礼物记录，支持分页")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "会话ID无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该会话"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/session/{sessionId}/list")
    public ResponseEntity<ApiResponse<Page<GiftRecordDTO>>> getSessionGiftsPaged(
            @Parameter(description = "会话ID", required = true, example = "sess_abc123") @PathVariable String sessionId,
            @Parameter(description = "页码，从0开始", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") int size) {
        Page<GiftRecordDTO> gifts = giftService.getSessionGiftsPaged(sessionId, page, size);
        return ResponseEntity.ok(ApiResponse.success(gifts));
    }
}
