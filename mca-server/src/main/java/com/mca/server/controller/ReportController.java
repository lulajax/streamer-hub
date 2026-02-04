package com.mca.server.controller;

import com.mca.server.dto.ApiResponse;
import com.mca.server.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "报表导出", description = "直播会话数据报表导出接口")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {
    
    private final ReportService reportService;
    
    @Operation(summary = "导出会话报表", description = "将会话数据导出为 Excel 文件")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "导出成功，返回 Excel 文件"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "会话ID无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该会话"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "会话不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误或导出失败")
    })
    @GetMapping("/session/{sessionId}/export")
    public ResponseEntity<byte[]> exportSessionReport(
            @Parameter(description = "会话ID", required = true, example = "sess_abc123") @PathVariable String sessionId) {
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
    
    @Operation(summary = "获取导出状态", description = "检查会话报表导出状态")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "会话ID无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问该会话"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/session/{sessionId}/export/status")
    public ResponseEntity<ApiResponse<String>> getExportStatus(
            @Parameter(description = "会话ID", required = true, example = "sess_abc123") @PathVariable String sessionId) {
        return ResponseEntity.ok(ApiResponse.success("Export ready"));
    }
}


