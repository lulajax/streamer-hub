package com.mca.server.repository;

import com.mca.server.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {
    
    Optional<Room> findByTiktokId(String tiktokId);
    
    List<Room> findByIsConnectedTrue();
    
    @Query("SELECT r FROM Room r WHERE r.isConnected = false ORDER BY r.disconnectedAt DESC")
    List<Room> findRecentRooms();
    
    boolean existsByTiktokId(String tiktokId);
}
