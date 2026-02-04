package com.mca.server.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mca.server.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserDTO", description = "用户信息")
public class UserDTO {

    @Schema(description = "用户ID", example = "user_abc123")
    private String id;

    @Schema(description = "用户邮箱", example = "user@example.com")
    private String email;

    @Schema(description = "用户昵称", example = "主播小明")
    private String nickname;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "邮箱是否已验证", example = "true")
    private Boolean isEmailVerified;

    @Schema(description = "是否已激活订阅", example = "true")
    private Boolean isActivated;

    @Schema(description = "激活码", example = "ABCD-EFGH-IJKL-MNOP")
    private String activationCode;

    @Schema(description = "订阅类型：BASIC-基础版, PREMIUM-高级版, ULTIMATE-旗舰版", example = "PREMIUM")
    private User.SubscriptionType subscriptionType;

    @Schema(description = "订阅过期时间", example = "2024-12-31 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime subscriptionExpiresAt;

    @Schema(description = "激活时间", example = "2024-01-15 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime activatedAt;

    @Schema(description = "最后登录时间", example = "2024-01-20 15:45:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;

    @Schema(description = "创建时间", example = "2024-01-10 08:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "访问令牌（JWT）", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;
    
    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .isEmailVerified(user.getIsEmailVerified())
                .isActivated(user.getIsActivated())
                .activationCode(user.getActivationCode())
                .subscriptionType(user.getSubscriptionType())
                .subscriptionExpiresAt(user.getSubscriptionExpiresAt())
                .activatedAt(user.getActivatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
    
    public static UserDTO fromEntityWithTokens(User user, String accessToken, String refreshToken) {
        UserDTO dto = fromEntity(user);
        dto.setAccessToken(accessToken);
        dto.setRefreshToken(refreshToken);
        return dto;
    }
}
