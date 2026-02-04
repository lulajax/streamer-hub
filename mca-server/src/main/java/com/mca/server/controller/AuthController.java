package com.mca.server.controller;

import com.mca.server.dto.*;
import com.mca.server.service.AuthService;
import com.mca.server.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "认证管理", description = "用户注册、登录、激活、密码重置等认证相关接口")
public class AuthController {
    
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    
    @Operation(summary = "用户注册", description = "新用户注册，需要提供邮箱、密码和设备信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "注册成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误或设备信息缺失"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "邮箱已被注册"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDTO>> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "注册信息", required = true)
            @Valid @RequestBody RegisterRequest request,
            @Parameter(description = "HTTP请求对象，用于获取设备信息", hidden = true) HttpServletRequest httpRequest) {
        // Set device info from request headers if not provided
        if (!StringUtils.hasText(request.getDeviceId())) {
            request.setDeviceId(httpRequest.getHeader("X-Device-Id"));
        }
        if (!StringUtils.hasText(request.getDeviceName())) {
            request.setDeviceName(httpRequest.getHeader("X-Device-Name"));
        }
        if (!StringUtils.hasText(request.getDeviceId())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Device ID is required"));
        }
        if (!StringUtils.hasText(request.getDeviceName())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Device name is required"));
        }
        return ResponseEntity.ok(authService.register(request));
    }
    
    @Operation(summary = "邮箱验证", description = "验证用户邮箱，通过点击邮件中的链接完成验证")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "邮箱验证成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "验证令牌无效或已过期"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<UserDTO>> verifyEmail(
            @Parameter(description = "邮箱验证令牌", example = "abc123xyz789") @RequestParam String token) {
        return ResponseEntity.ok(authService.verifyEmail(token));
    }
    
    @Operation(summary = "用户登录", description = "使用邮箱和密码登录，返回用户信息和 JWT Token")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登录成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误或设备信息缺失"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "邮箱或密码错误"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDTO>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "登录信息", required = true)
            @Valid @RequestBody LoginRequest request,
            @Parameter(description = "HTTP请求对象，用于获取设备信息", hidden = true) HttpServletRequest httpRequest) {
        // Set device info from request headers if not provided
        if (!StringUtils.hasText(request.getDeviceId())) {
            request.setDeviceId(httpRequest.getHeader("X-Device-Id"));
        }
        if (!StringUtils.hasText(request.getDeviceName())) {
            request.setDeviceName(httpRequest.getHeader("X-Device-Name"));
        }
        if (!StringUtils.hasText(request.getDeviceId())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Device ID is required"));
        }
        if (!StringUtils.hasText(request.getDeviceName())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Device name is required"));
        }
        return ResponseEntity.ok(authService.login(request));
    }
    
    @Operation(summary = "激活高级订阅", description = "使用激活码激活高级订阅，需要登录状态")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "激活成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "激活码无效或已过期"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/activate")
    public ResponseEntity<ApiResponse<UserDTO>> activatePremium(
            @Parameter(description = "授权头，格式：Bearer {token}", required = true, example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
            @RequestHeader("Authorization") String authHeader,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "激活订阅请求", required = true)
            @Valid @RequestBody ActivatePremiumRequest request) {
        String userId = extractUserIdFromToken(authHeader);
        return ResponseEntity.ok(authService.activatePremium(userId, request));
    }
    
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(
            @Parameter(description = "授权头，格式：Bearer {token}", required = true, example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
            @RequestHeader("Authorization") String authHeader) {
        String userId = extractUserIdFromToken(authHeader);
        return ResponseEntity.ok(authService.getCurrentUser(userId));
    }
    
    @Operation(summary = "请求密码重置", description = "发送密码重置邮件到指定邮箱")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "重置邮件已发送（无论邮箱是否存在都返回成功，防止枚举攻击）"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "邮箱格式无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
            @Parameter(description = "用户邮箱地址", required = true, example = "user@example.com") @RequestParam String email) {
        return ResponseEntity.ok(authService.requestPasswordReset(email));
    }
    
    @Operation(summary = "重置密码", description = "使用重置令牌设置新密码")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "密码重置成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "令牌无效或密码格式不符合要求"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Parameter(description = "密码重置令牌", required = true, example = "reset_token_abc123") @RequestParam String token,
            @Parameter(description = "新密码，至少8位字符", required = true, example = "newPassword123") @RequestParam String newPassword) {
        return ResponseEntity.ok(authService.resetPassword(token, newPassword));
    }
    
    @Operation(summary = "生成激活码", description = "生成新的订阅激活码（管理员功能）")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "激活码生成成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或 Token 无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限访问"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "订阅类型无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/generate-code")
    public ResponseEntity<ApiResponse<String>> generateActivationCode(
            @Parameter(description = "订阅类型，可选值：BASIC, PREMIUM, ULTIMATE", example = "PREMIUM")
            @RequestParam(defaultValue = "BASIC") String type) {
        return ResponseEntity.ok(authService.generateActivationCode(type));
    }
    
    private String extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("Invalid or expired token");
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}


