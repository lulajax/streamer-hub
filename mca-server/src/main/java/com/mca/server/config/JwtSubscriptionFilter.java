package com.mca.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mca.server.dto.ApiResponse;
import com.mca.server.entity.User;
import com.mca.server.repository.UserRepository;
import com.mca.server.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtSubscriptionFilter extends OncePerRequestFilter {

    // Public endpoints should bypass JWT + subscription checks (e.g., auth + Swagger docs)
    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/",
            "/public/",
            "/ws/",
            "/actuator/",
            "/swagger-ui/",
            "/swagger-ui.html",
            "/v3/api-docs",
            "/swagger-resources/",
            "/webjars/"
    );

    private static final List<String> SUBSCRIPTION_PATHS = List.of(
            "/sessions",
            "/sessions/",
            "/gifts",
            "/gifts/",
            "/widget",
            "/widget/",
            "/reports",
            "/reports/",
            "/presets",
            "/presets/",
            "/anchors",
            "/anchors/"
    );

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = normalizePath(request);
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            respondUnauthorized(response, "Missing authorization token");
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            respondUnauthorized(response, "Invalid or expired token");
            return;
        }

        String userId = jwtUtil.getUserIdFromToken(token);
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            respondUnauthorized(response, "User not found");
            return;
        }

        User user = userOpt.get();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId, null, List.of()
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        if (requiresSubscription(path) && !user.hasActiveSubscription()) {
            respondForbidden(response, "Subscription required or expired");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean requiresSubscription(String path) {
        return SUBSCRIPTION_PATHS.stream().anyMatch(path::startsWith);
    }

    private String normalizePath(HttpServletRequest request) {
        String path = extractPathWithinApplication(request);
        if ("/api".equals(path)) {
            return "/";
        }
        if (path.startsWith("/api/")) {
            return path.substring(4);
        }
        return path;
    }

    private String extractPathWithinApplication(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath == null || contextPath.isEmpty()) {
            return requestUri;
        }
        if (!requestUri.startsWith(contextPath)) {
            return requestUri;
        }
        String path = requestUri.substring(contextPath.length());
        return path.isEmpty() ? "/" : path;
    }

    private void respondUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        writeResponse(response, ApiResponse.error(message));
    }

    private void respondForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        writeResponse(response, ApiResponse.error(message));
    }

    private void writeResponse(HttpServletResponse response, ApiResponse<?> apiResponse) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
