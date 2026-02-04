package com.mca.server.controller;

import com.mca.server.dto.ActivationRequest;
import com.mca.server.dto.ActivationResponse;
import com.mca.server.dto.ApiResponse;
import com.mca.server.service.ActivationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/activation")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "设备激活", description = "设备激活码验证和管理接口")
public class ActivationController {
    
    private final ActivationService activationService;
    
    @Operation(summary = "激活设备", description = "使用激活码激活设备，绑定设备到订阅")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "设备激活成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "激活码无效或已过期，或设备ID缺失"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "激活码已被使用或设备数量超限"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ActivationResponse>> activate(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "激活请求", required = true)
            @Valid @RequestBody ActivationRequest request) {
        return ResponseEntity.ok(activationService.activate(request));
    }
    
    @Operation(summary = "检查激活状态", description = "检查指定设备的激活状态和订阅有效期")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "设备ID无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/check/{deviceId}")
    public ResponseEntity<ApiResponse<ActivationResponse>> checkActivation(
            @Parameter(description = "设备唯一标识", required = true, example = "device_abc123_xyz") @PathVariable String deviceId) {
        return ResponseEntity.ok(activationService.checkActivation(deviceId));
    }
    
    @Operation(summary = "停用设备", description = "停用指定设备的激活状态（管理员功能）")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "设备停用成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "设备ID无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "设备未找到"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/deactivate/{deviceId}")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @Parameter(description = "设备唯一标识", required = true, example = "device_abc123_xyz") @PathVariable String deviceId) {
        return ResponseEntity.ok(activationService.deactivate(deviceId));
    }
    
    @Operation(summary = "生成激活码", description = "生成新的设备激活码（管理员功能）")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "激活码生成成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<String>> generateCode() {
        String code = activationService.generateActivationCode();
        return ResponseEntity.ok(ApiResponse.success("Activation code generated", code));
    }
}


