package com.mca.server.service;

import com.mca.server.entity.GiftRecord;
import com.mca.server.entity.Session;
import com.mca.server.repository.GiftRecordRepository;
import com.mca.server.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
    
    private final SessionRepository sessionRepository;
    private final GiftRecordRepository giftRecordRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public byte[] generateSessionReport(String sessionId) throws IOException {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        List<GiftRecord> gifts = giftRecordRepository.findBySessionIdOrderByCreatedAtDesc(sessionId);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            // Summary Sheet
            createSummarySheet(workbook, session);
            
            // Gift Records Sheet
            createGiftRecordsSheet(workbook, gifts);
            
            // Gift Statistics Sheet
            createGiftStatsSheet(workbook, sessionId);
            
            // Top Users Sheet
            createTopUsersSheet(workbook, sessionId);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    private void createSummarySheet(Workbook workbook, Session session) {
        Sheet sheet = workbook.createSheet("Summary");
        
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Session Report");
        
        int rowNum = 2;
        createSummaryRow(sheet, rowNum++, "Session ID", session.getId());
        createSummaryRow(sheet, rowNum++, "Room ID", session.getRoom().getTiktokId());
        createSummaryRow(sheet, rowNum++, "Game Mode", session.getGameMode().toString());
        createSummaryRow(sheet, rowNum++, "Status", session.getStatus().toString());
        createSummaryRow(sheet, rowNum++, "Started At", 
                session.getStartedAt() != null ? session.getStartedAt().format(DATE_FORMATTER) : "N/A");
        createSummaryRow(sheet, rowNum++, "Ended At", 
                session.getEndedAt() != null ? session.getEndedAt().format(DATE_FORMATTER) : "N/A");
        createSummaryRow(sheet, rowNum++, "Total Rounds", String.valueOf(session.getCurrentRound()));
        createSummaryRow(sheet, rowNum++, "Total Gifts", String.valueOf(session.getTotalGifts()));
        createSummaryRow(sheet, rowNum++, "Total Diamonds", String.valueOf(session.getTotalDiamonds()));
        
        // Auto-size columns
        for (int i = 0; i < 2; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createSummaryRow(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }
    
    private void createGiftRecordsSheet(Workbook workbook, List<GiftRecord> gifts) {
        Sheet sheet = workbook.createSheet("Gift Records");
        
        // Header
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Time", "User", "Gift", "Quantity", "Diamond Cost", "Total", "Anchor", "Bind Type"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        
        // Data
        int rowNum = 1;
        for (GiftRecord gift : gifts) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(gift.getCreatedAt().format(DATE_FORMATTER));
            row.createCell(1).setCellValue(gift.getUserName());
            row.createCell(2).setCellValue(gift.getGiftName());
            row.createCell(3).setCellValue(gift.getQuantity());
            row.createCell(4).setCellValue(gift.getDiamondCost());
            row.createCell(5).setCellValue(gift.getTotalCost());
            row.createCell(6).setCellValue(gift.getAnchorName() != null ? gift.getAnchorName() : "N/A");
            row.createCell(7).setCellValue(gift.getBindType().toString());
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createGiftStatsSheet(Workbook workbook, String sessionId) {
        Sheet sheet = workbook.createSheet("Gift Statistics");
        
        List<Object[]> stats = giftRecordRepository.getGiftStatsBySessionId(sessionId);
        
        // Header
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Gift Name");
        headerRow.createCell(1).setCellValue("Total Count");
        headerRow.createCell(2).setCellValue("Total Value");
        
        // Data
        int rowNum = 1;
        for (Object[] stat : stats) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue((String) stat[1]);
            row.createCell(1).setCellValue(((Number) stat[2]).longValue());
            row.createCell(2).setCellValue(((Number) stat[3]).longValue());
        }
        
        // Auto-size columns
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createTopUsersSheet(Workbook workbook, String sessionId) {
        Sheet sheet = workbook.createSheet("Top Users");
        
        List<Object[]> topUsers = giftRecordRepository.getTopUsersBySessionId(sessionId, PageRequest.of(0, 50));
        
        // Header
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Rank");
        headerRow.createCell(1).setCellValue("User Name");
        headerRow.createCell(2).setCellValue("Total Spent");
        headerRow.createCell(3).setCellValue("Gift Count");
        
        // Data
        int rowNum = 1;
        int rank = 1;
        for (Object[] user : topUsers) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rank++);
            row.createCell(1).setCellValue((String) user[1]);
            row.createCell(2).setCellValue(((Number) user[2]).longValue());
            row.createCell(3).setCellValue(((Number) user[3]).longValue());
        }
        
        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
