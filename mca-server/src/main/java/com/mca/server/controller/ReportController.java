package com.mca.server.controller;

import com.mca.server.dto.ApiResponse;
import com.mca.server.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportController {
    
    private final ReportService reportService;
    
    @GetMapping("/session/{sessionId}/export")
    public ResponseEntity<byte[]> exportSessionReport(@PathVariable String sessionId) {
        try {
            byte[] reportData = reportService.generateSessionReport(sessionId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "session_report_" + sessionId + ".xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(reportData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/session/{sessionId}/export/status")
    public ResponseEntity<ApiResponse<String>> getExportStatus(@PathVariable String sessionId) {
        return ResponseEntity.ok(ApiResponse.success("Export ready"));
    }
}
