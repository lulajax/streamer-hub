package com.mca.server.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WidgetUpdateEvent {
    private String token;
    private String sessionId;
    private String roomId;
    private String presetId;
}
