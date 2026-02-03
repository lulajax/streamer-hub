package com.mca.server.service;

import com.mca.server.dto.GiftRecordDTO;
import com.mca.server.entity.GiftRecord;
import com.mca.server.entity.Session;
import com.mca.server.repository.GiftRecordRepository;
import com.mca.server.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftService {
    
    private final GiftRecordRepository giftRecordRepository;
    private final SessionRepository sessionRepository;
    
    @Transactional
    public GiftRecordDTO recordGift(String sessionId, GiftRecordDTO giftDTO) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        GiftRecord giftRecord = GiftRecord.builder()
                .giftId(giftDTO.getGiftId())
                .giftName(giftDTO.getGiftName())
                .giftIcon(giftDTO.getGiftIcon())
                .userId(giftDTO.getUserId())
                .userName(giftDTO.getUserName())
                .userAvatar(giftDTO.getUserAvatar())
                .anchorId(giftDTO.getAnchorId())
                .anchorName(giftDTO.getAnchorName())
                .quantity(giftDTO.getQuantity())
                .diamondCost(giftDTO.getDiamondCost())
                .totalCost(giftDTO.getTotalCost())
                .bindType(giftDTO.getBindType() != null ? giftDTO.getBindType() : GiftRecord.BindType.NONE)
                .session(session)
                .build();
        
        GiftRecord saved = giftRecordRepository.save(giftRecord);
        
        // Update session totals
        session.addGiftRecord(saved);
        sessionRepository.save(session);
        
        log.info("Gift recorded: {} x{} from {} to {}", 
                giftDTO.getGiftName(), giftDTO.getQuantity(), 
                giftDTO.getUserName(), giftDTO.getAnchorName());
        
        return GiftRecordDTO.fromEntity(saved);
    }
    
    @Transactional(readOnly = true)
    public List<GiftRecordDTO> getSessionGifts(String sessionId) {
        return giftRecordRepository.findBySessionIdOrderByCreatedAtDesc(sessionId)
                .stream()
                .map(GiftRecordDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<GiftRecordDTO> getSessionGiftsByType(String sessionId, GiftRecord.BindType bindType) {
        return giftRecordRepository.findBySessionIdAndBindType(sessionId, bindType)
                .stream()
                .map(GiftRecordDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public GiftRecordDTO updateGiftBinding(String giftId, String anchorId, String anchorName, GiftRecord.BindType bindType) {
        GiftRecord gift = giftRecordRepository.findById(giftId)
                .orElseThrow(() -> new RuntimeException("Gift record not found"));
        
        gift.setAnchorId(anchorId);
        gift.setAnchorName(anchorName);
        gift.setBindType(bindType);
        
        GiftRecord updated = giftRecordRepository.save(gift);
        
        log.info("Gift binding updated: {} -> {}", giftId, anchorName);
        
        return GiftRecordDTO.fromEntity(updated);
    }
    
    @Transactional(readOnly = true)
    public List<GiftRecordDTO> getTopGifts(String sessionId, int limit) {
        return giftRecordRepository.findBySessionId(sessionId, PageRequest.of(0, limit))
                .getContent()
                .stream()
                .map(GiftRecordDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public long getSessionGiftCount(String sessionId) {
        return giftRecordRepository.countBySessionId(sessionId);
    }
    
    @Transactional(readOnly = true)
    public Long getSessionTotalDiamonds(String sessionId) {
        return giftRecordRepository.sumTotalCostBySessionId(sessionId);
    }
}
