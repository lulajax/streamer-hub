package com.mca.server.controller;

import com.mca.server.dto.ApiResponse;
import com.mca.server.dto.GiftRecordDTO;
import com.mca.server.entity.GiftRecord;
import com.mca.server.service.GiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gifts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GiftController {
    
    private final GiftService giftService;
    
    @PostMapping("/session/{sessionId}")
    public ResponseEntity<ApiResponse<GiftRecordDTO>> recordGift(
            @PathVariable String sessionId,
            @RequestBody GiftRecordDTO giftDTO) {
        GiftRecordDTO recorded = giftService.recordGift(sessionId, giftDTO);
        return ResponseEntity.ok(ApiResponse.success("Gift recorded", recorded));
    }
    
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ApiResponse<List<GiftRecordDTO>>> getSessionGifts(
            @PathVariable String sessionId) {
        List<GiftRecordDTO> gifts = giftService.getSessionGifts(sessionId);
        return ResponseEntity.ok(ApiResponse.success(gifts));
    }
    
    @GetMapping("/session/{sessionId}/type/{bindType}")
    public ResponseEntity<ApiResponse<List<GiftRecordDTO>>> getSessionGiftsByType(
            @PathVariable String sessionId,
            @PathVariable GiftRecord.BindType bindType) {
        List<GiftRecordDTO> gifts = giftService.getSessionGiftsByType(sessionId, bindType);
        return ResponseEntity.ok(ApiResponse.success(gifts));
    }
    
    @PutMapping("/{giftId}/bind")
    public ResponseEntity<ApiResponse<GiftRecordDTO>> updateGiftBinding(
            @PathVariable String giftId,
            @RequestParam String anchorId,
            @RequestParam String anchorName,
            @RequestParam GiftRecord.BindType bindType) {
        GiftRecordDTO updated = giftService.updateGiftBinding(giftId, anchorId, anchorName, bindType);
        return ResponseEntity.ok(ApiResponse.success("Gift binding updated", updated));
    }
    
    @GetMapping("/session/{sessionId}/top")
    public ResponseEntity<ApiResponse<List<GiftRecordDTO>>> getTopGifts(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "20") int limit) {
        List<GiftRecordDTO> gifts = giftService.getTopGifts(sessionId, limit);
        return ResponseEntity.ok(ApiResponse.success(gifts));
    }
    
    @GetMapping("/session/{sessionId}/count")
    public ResponseEntity<ApiResponse<Long>> getSessionGiftCount(
            @PathVariable String sessionId) {
        long count = giftService.getSessionGiftCount(sessionId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    
    @GetMapping("/session/{sessionId}/total-diamonds")
    public ResponseEntity<ApiResponse<Long>> getSessionTotalDiamonds(
            @PathVariable String sessionId) {
        Long total = giftService.getSessionTotalDiamonds(sessionId);
        return ResponseEntity.ok(ApiResponse.success(total));
    }
}
