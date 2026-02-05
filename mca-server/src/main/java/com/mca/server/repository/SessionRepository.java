package com.mca.server.repository;

import com.mca.server.entity.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, String> {
    
    List<Session> findByRoomIdOrderByCreatedAtDesc(String roomId);
    
    Page<Session> findByRoomId(String roomId, Pageable pageable);
    
    Optional<Session> findByRoomIdAndStatus(String roomId, Session.SessionStatus status);

    Optional<Session> findByWidgetToken(String widgetToken);

    Optional<Session> findTopByPresetIdOrderByCreatedAtDesc(String presetId);
    
    @Query("SELECT s FROM Session s WHERE s.room.id = :roomId AND s.status != 'ENDED' ORDER BY s.createdAt DESC")
    List<Session> findActiveSessionsByRoomId(@Param("roomId") String roomId);
}
