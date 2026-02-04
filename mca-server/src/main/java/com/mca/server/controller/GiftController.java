package com.mca.server.controller;

import com.mca.server.dto.ApiResponse;
import com.mca.server.dto.GiftRecordDTO;
import com.mca.server.entity.GiftRecord;
import com.mca.server.service.GiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
    
    @Operation(summary = "按类型获取礼物", description = "获取指定会话中特定绑定类型的礼物记录")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该会话"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/session/{sessionId}/type/{bindType}")
    public ResponseEntity<ApiResponse<List<GiftRecordDTO>>> getSessionGiftsByType(
            @Parameter(description = "会话ID", required = true, example = "sess_abc123") @PathVariable String sessionId,
            @Parameter(description = "绑定类型", required = true, example = "BOUND") @PathVariable GiftRecord.BindType bindType) {
        List<GiftRecordDTO> gifts = giftService.getSessionGiftsByType(sessionId, bindType);
        return ResponseEntity.ok(ApiResponse.success(gifts));
    }
    
    @Operation(summary = "更新礼物绑定", description = "更新礼物的绑定主播和绑定类型")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "绑定更新成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该会话"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "礼物记录不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PutMapping("/{giftId}/bind")
    public ResponseEntity<ApiResponse<GiftRecordDTO>> updateGiftBinding(
            @Parameter(description = "礼物记录ID", required = true, example = "gift_abc123") @PathVariable String giftId,
            @Parameter(description = "主播ID", required = true, example = "anchor_xyz789") @RequestParam String anchorId,
            @Parameter(description = "主播名称", required = true, example = "主播小明") @RequestParam String anchorName,
            @Parameter(description = "绑定类型：UNBOUND-未绑定, BOUND-已绑定, PENDING-待确认", required = true, example = "BOUND") @RequestParam GiftRecord.BindType bindType) {
        GiftRecordDTO updated = giftService.updateGiftBinding(giftId, anchorId, anchorName, bindType);
        return ResponseEntity.ok(ApiResponse.success("Gift binding updated", updated));
    }
    
    @Operation(summary = "获取热门礼物", description = "获取指定会话的热门礼物排行")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "会话ID无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该会话"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/session/{sessionId}/top")
    public ResponseEntity<ApiResponse<List<GiftRecordDTO>>> getTopGifts(
            @Parameter(description = "会话ID", required = true, example = "sess_abc123") @PathVariable String sessionId,
            @Parameter(description = "返回数量限制", example = "20") @RequestParam(defaultValue = "20") int limit) {
        List<GiftRecordDTO> gifts = giftService.getTopGifts(sessionId, limit);
        return ResponseEntity.ok(ApiResponse.success(gifts));
    }
    
    @Operation(summary = "获取礼物数量", description = "获取指定会话的礼物总数量")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "会话ID无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该会话"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/session/{sessionId}/count")
    public ResponseEntity<ApiResponse<Long>> getSessionGiftCount(
            @Parameter(description = "会话ID", required = true, example = "sess_abc123") @PathVariable String sessionId) {
        long count = giftService.getSessionGiftCount(sessionId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    
    @Operation(summary = "获取钻石总数", description = "获取指定会话的钻石总消耗数")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "会话ID无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该会话"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/session/{sessionId}/total-diamonds")
    public ResponseEntity<ApiResponse<Long>> getSessionTotalDiamonds(
            @Parameter(description = "会话ID", required = true, example = "sess_abc123") @PathVariable String sessionId) {
        Long total = giftService.getSessionTotalDiamonds(sessionId);
        return ResponseEntity.ok(ApiResponse.success(total));
    }
}


