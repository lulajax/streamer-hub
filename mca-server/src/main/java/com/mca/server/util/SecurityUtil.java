package com.mca.server.util;

import com.mca.server.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessException("用户未认证");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof String userId && !userId.isBlank()) {
            return userId;
        }
        if (principal instanceof CharSequence sequence && sequence.length() > 0) {
            return sequence.toString();
        }
        throw new BusinessException("用户未认证");
    }
}
