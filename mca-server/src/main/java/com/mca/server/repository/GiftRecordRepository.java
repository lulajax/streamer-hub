package com.mca.server.repository;

import com.mca.server.entity.GiftRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GiftRecordRepository extends JpaRepository<GiftRecord, String> {
    
    List<GiftRecord> findBySessionIdOrderByCreatedAtDesc(String sessionId);
    
    Page<GiftRecord> findBySessionId(String sessionId, Pageable pageable);
    
    List<GiftRecord> findByUserId(String userId);
    
    List<GiftRecord> findByAnchorId(String anchorId);
    
    @Query("SELECT gr FROM GiftRecord gr WHERE gr.session.id = :sessionId AND gr.bindType = :bindType")
    List<GiftRecord> findBySessionIdAndBindType(@Param("sessionId") String sessionId, 
                                                 @Param("bindType") GiftRecord.BindType bindType);
    
    @Query("SELECT SUM(gr.totalCost) FROM GiftRecord gr WHERE gr.session.id = :sessionId")
    Long sumTotalCostBySessionId(@Param("sessionId") String sessionId);
    
    @Query("SELECT gr.giftId, gr.giftName, SUM(gr.quantity), SUM(gr.totalCost) " +
           "FROM GiftRecord gr WHERE gr.session.id = :sessionId GROUP BY gr.giftId, gr.giftName")
    List<Object[]> getGiftStatsBySessionId(@Param("sessionId") String sessionId);
    
    @Query("SELECT gr.userId, gr.userName, SUM(gr.totalCost), COUNT(gr) " +
           "FROM GiftRecord gr WHERE gr.session.id = :sessionId GROUP BY gr.userId, gr.userName " +
           "ORDER BY SUM(gr.totalCost) DESC")
    List<Object[]> getTopUsersBySessionId(@Param("sessionId") String sessionId, Pageable pageable);
    
    long countBySessionId(String sessionId);
    
    @Query("SELECT gr FROM GiftRecord gr WHERE gr.createdAt >= :startTime AND gr.createdAt <= :endTime")
    List<GiftRecord> findByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                      @Param("endTime") LocalDateTime endTime);
}
