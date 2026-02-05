package com.mca.server.config;

import com.mca.server.websocket.RoomWebSocketHandler;
import com.mca.server.websocket.WidgetWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    
    private final RoomWebSocketHandler roomWebSocketHandler;
    private final WidgetWebSocketHandler widgetWebSocketHandler;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Room state WebSocket - for producer/consumer pattern
        registry.addHandler(roomWebSocketHandler, "/ws/room")
                .setAllowedOrigins("*");

        // Widget WebSocket - for widget token based overlays
        registry.addHandler(widgetWebSocketHandler, "/ws/widget/{token}")
                .setAllowedOrigins("*");
    }
}
