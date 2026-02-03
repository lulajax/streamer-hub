package com.mca.server.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mca.server.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    
    private String id;
    private String email;
    private String nickname;
    private String avatarUrl;
    private Boolean isEmailVerified;
    private Boolean isActivated;
    private User.SubscriptionType subscriptionType;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime subscriptionExpiresAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime activatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    // Token for authenticated requests
    private String accessToken;
    private String refreshToken;
    
    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .isEmailVerified(user.getIsEmailVerified())
                .isActivated(user.getIsActivated())
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
