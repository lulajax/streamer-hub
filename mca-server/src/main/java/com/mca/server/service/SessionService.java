package com.mca.server.service;

import com.mca.server.dto.SessionDTO;
import com.mca.server.entity.Preset;
import com.mca.server.entity.Room;
import com.mca.server.entity.Session;
import com.mca.server.repository.PresetRepository;
import com.mca.server.repository.RoomRepository;
import com.mca.server.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {
    
    private final SessionRepository sessionRepository;
    private final RoomRepository roomRepository;
    private final PresetRepository presetRepository;
    
    @Transactional
    public SessionDTO createSession(String roomId, String presetId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Preset preset = presetRepository.findById(presetId)
                .orElseThrow(() -> new RuntimeException("Preset not found"));

        List<Session> activeSessions = sessionRepository.findActiveSessionsByRoomId(roomId);
        if (!activeSessions.isEmpty()) {
            throw new RuntimeException("Active session already exists for this room");
        }

        Session session = Session.builder()
                .room(room)
                .preset(preset)
                .gameMode(preset.getGameMode())
                .widgetToken("sess_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16))
                .widgetSettingsSnapshot(preset.getWidgetSettingsJson())
                .status(Session.SessionStatus.IDLE)
                .currentRound(1)
                .totalGifts(0L)
                .totalDiamonds(0L)
                .build();

        Session saved = sessionRepository.save(session);

        log.info("Session created: {} for room: {} with preset: {}", saved.getId(), roomId, presetId);

        return SessionDTO.fromEntity(saved);
    }

    @Transactional
    public SessionDTO quickStartWithDefault(String roomId, String deviceId) {
        Preset defaultPreset = presetRepository.findByDeviceIdAndIsDefaultTrue(deviceId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No default preset found for device"));

        return createSession(roomId, defaultPreset.getId());
    }

    @Transactional(readOnly = true)
    public String getWidgetUrl(String sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        return "/widget/" + session.getWidgetToken() + "?mode=session";
    }
    
    @Transactional
    public SessionDTO startSession(String sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        if (session.getStatus() != Session.SessionStatus.IDLE && 
            session.getStatus() != Session.SessionStatus.PAUSED) {
            throw new RuntimeException("Session cannot be started");
        }
        
        session.setStatus(Session.SessionStatus.RUNNING);
        if (session.getStartedAt() == null) {
            session.setStartedAt(LocalDateTime.now());
        }
        
        Session saved = sessionRepository.save(session);
        
        log.info("Session started: {}", sessionId);
        
        return SessionDTO.fromEntity(saved);
    }
    
    @Transactional
    public SessionDTO pauseSession(String sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        if (session.getStatus() != Session.SessionStatus.RUNNING) {
            throw new RuntimeException("Session is not running");
        }
        
        session.setStatus(Session.SessionStatus.PAUSED);
        session.setPausedAt(LocalDateTime.now());
        
        Session saved = sessionRepository.save(session);
        
        log.info("Session paused: {}", sessionId);
        
        return SessionDTO.fromEntity(saved);
    }
    
    @Transactional
    public SessionDTO endSession(String sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        session.setStatus(Session.SessionStatus.ENDED);
        session.setEndedAt(LocalDateTime.now());
        
        Session saved = sessionRepository.save(session);
        
        log.info("Session ended: {}", sessionId);
        
        return SessionDTO.fromEntity(saved);
    }
    
    @Transactional
    public SessionDTO nextRound(String sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        session.setCurrentRound(session.getCurrentRound() + 1);
        
        Session saved = sessionRepository.save(session);
        
        log.info("Session {} advanced to round {}", sessionId, saved.getCurrentRound());
        
        return SessionDTO.fromEntity(saved);
    }
    
    @Transactional(readOnly = true)
    public SessionDTO getSession(String sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        return SessionDTO.fromEntity(session);
    }
    
    @Transactional(readOnly = true)
    public List<SessionDTO> getRoomSessions(String roomId) {
        return sessionRepository.findByRoomIdOrderByCreatedAtDesc(roomId)
                .stream()
                .map(SessionDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public SessionDTO getActiveSession(String roomId) {
        return sessionRepository.findByRoomIdAndStatus(roomId, Session.SessionStatus.RUNNING)
                .map(SessionDTO::fromEntity)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public SessionDTO getSessionByWidgetToken(String widgetToken) {
        return sessionRepository.findByWidgetToken(widgetToken)
                .map(SessionDTO::fromEntity)
                .orElseThrow(() -> new RuntimeException("Session not found"));
    }
}
