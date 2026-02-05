package com.mca.server.service;

import com.mca.server.dto.GiftRecordDTO;
import com.mca.server.entity.GiftRecord;
import com.mca.server.entity.Session;
import com.mca.server.repository.GiftRecordRepository;
import com.mca.server.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
                .anchorAvatar(giftDTO.getAnchorAvatar())
                .toMemberId(giftDTO.getToMemberId())
                .toMemberNickname(giftDTO.getToMemberNickname())
                .messageId(giftDTO.getMessageId())
                .messageTimeStamp(giftDTO.getMessageTimeStamp())
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
    public Page<GiftRecordDTO> getSessionGiftsPaged(String sessionId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return giftRecordRepository.findBySessionId(sessionId, pageable)
                .map(GiftRecordDTO::fromEntity);
    }
}
