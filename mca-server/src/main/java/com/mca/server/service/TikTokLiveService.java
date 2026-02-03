package com.mca.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Service to interact with TikTok Live via TikTok-Live-Connector
 * This service provides methods to:
 * - Check if a user is live
 * - Get room information
 * - Get available gifts
 * - Connect to live stream and receive events
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TikTokLiveService {
    
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${mca.tiktok.sign-api-key:}")
    private String signApiKey;
    
    // TikTok-Live-Connector uses Euler Stream API for signing
    private static final String EULER_STREAM_API = "https://tiktok.eulerstream.com";
    
    /**
     * Check if a TikTok user is currently live
     */
    public boolean isUserLive(String uniqueId) {
        try {
            String url = EULER_STREAM_API + "/webcast/fetchRoomId?uniqueId=" + uniqueId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                return jsonNode.has("roomId") && !jsonNode.get("roomId").asText().isEmpty();
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to check if user {} is live", uniqueId, e);
            return false;
        }
    }
    
    /**
     * Get room information for a TikTok user
     */
    public JsonNode getRoomInfo(String uniqueId) {
        try {
            String url = EULER_STREAM_API + "/webcast/fetchRoomInfo?uniqueId=" + uniqueId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readTree(response.getBody());
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to get room info for user {}", uniqueId, e);
            return null;
        }
    }
    
    /**
     * Get room ID for a TikTok user
     */
    public String getRoomId(String uniqueId) {
        try {
            String url = EULER_STREAM_API + "/webcast/fetchRoomId?uniqueId=" + uniqueId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                if (jsonNode.has("roomId")) {
                    return jsonNode.get("roomId").asText();
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to get room ID for user {}", uniqueId, e);
            return null;
        }
    }
    
    /**
     * Get available gifts for a room
     */
    public ArrayNode getAvailableGifts(String roomId) {
        try {
            String url = EULER_STREAM_API + "/webcast/fetchAvailableGifts?roomId=" + roomId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                if (jsonNode.isArray()) {
                    return (ArrayNode) jsonNode;
                }
            }
            return objectMapper.createArrayNode();
        } catch (Exception e) {
            log.error("Failed to get available gifts for room {}", roomId, e);
            return objectMapper.createArrayNode();
        }
    }
    
    /**
     * Get signed WebSocket URL for connecting to TikTok Live
     */
    public String getSignedWebSocketUrl(String uniqueId, String roomId) {
        try {
            String url = EULER_STREAM_API + "/webcast/fetchSignedWebSocket?uniqueId=" + uniqueId;
            if (roomId != null) {
                url += "&roomId=" + roomId;
            }
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                if (jsonNode.has("websocketUrl")) {
                    return jsonNode.get("websocketUrl").asText();
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to get signed WebSocket URL for user {}", uniqueId, e);
            return null;
        }
    }
    
    /**
     * Parse gift data from TikTok-Live-Connector format to our format
     */
    public Map<String, Object> parseGiftData(JsonNode giftData) {
        Map<String, Object> parsed = new HashMap<>();
        
        try {
            // Extract user info
            if (giftData.has("user")) {
                JsonNode user = giftData.get("user");
                parsed.put("userId", user.has("userId") ? user.get("userId").asText() : "");
                parsed.put("userName", user.has("uniqueId") ? user.get("uniqueId").asText() : "");
                parsed.put("userNickname", user.has("nickname") ? user.get("nickname").asText() : "");
                parsed.put("userAvatar", user.has("avatarThumb") ? user.get("avatarThumb").asText() : "");
            }
            
            // Extract gift info
            if (giftData.has("gift")) {
                JsonNode gift = giftData.get("gift");
                parsed.put("giftId", gift.has("giftId") ? gift.get("giftId").asText() : "");
                parsed.put("giftName", gift.has("name") ? gift.get("name").asText() : "");
                parsed.put("giftIcon", gift.has("image") ? gift.get("image").asText() : "");
                parsed.put("diamondCost", gift.has("diamondCount") ? gift.get("diamondCount").asInt() : 0);
            }
            
            // Extract quantity and repeat info
            parsed.put("quantity", giftData.has("repeatCount") ? giftData.get("repeatCount").asInt() : 1);
            parsed.put("repeatEnd", giftData.has("repeatEnd") ? giftData.get("repeatEnd").asBoolean() : true);
            parsed.put("giftType", giftData.has("giftType") ? giftData.get("giftType").asInt() : 1);
            
            // Calculate total cost
            int diamondCost = (int) parsed.getOrDefault("diamondCost", 0);
            int quantity = (int) parsed.getOrDefault("quantity", 1);
            parsed.put("totalCost", diamondCost * quantity);
            
        } catch (Exception e) {
            log.error("Failed to parse gift data", e);
        }
        
        return parsed;
    }
    
    /**
     * Parse chat message data
     */
    public Map<String, Object> parseChatData(JsonNode chatData) {
        Map<String, Object> parsed = new HashMap<>();
        
        try {
            if (chatData.has("user")) {
                JsonNode user = chatData.get("user");
                parsed.put("userId", user.has("userId") ? user.get("userId").asText() : "");
                parsed.put("userName", user.has("uniqueId") ? user.get("uniqueId").asText() : "");
                parsed.put("userNickname", user.has("nickname") ? user.get("nickname").asText() : "");
                parsed.put("userAvatar", user.has("avatarThumb") ? user.get("avatarThumb").asText() : "");
            }
            
            parsed.put("comment", chatData.has("comment") ? chatData.get("comment").asText() : "");
            
        } catch (Exception e) {
            log.error("Failed to parse chat data", e);
        }
        
        return parsed;
    }
    
    /**
     * Parse member join data
     */
    public Map<String, Object> parseMemberData(JsonNode memberData) {
        Map<String, Object> parsed = new HashMap<>();
        
        try {
            if (memberData.has("user")) {
                JsonNode user = memberData.get("user");
                parsed.put("userId", user.has("userId") ? user.get("userId").asText() : "");
                parsed.put("userName", user.has("uniqueId") ? user.get("uniqueId").asText() : "");
                parsed.put("userNickname", user.has("nickname") ? user.get("nickname").asText() : "");
                parsed.put("userAvatar", user.has("avatarThumb") ? user.get("avatarThumb").asText() : "");
            }
            
        } catch (Exception e) {
            log.error("Failed to parse member data", e);
        }
        
        return parsed;
    }
    
    /**
     * Parse like data
     */
    public Map<String, Object> parseLikeData(JsonNode likeData) {
        Map<String, Object> parsed = new HashMap<>();
        
        try {
            if (likeData.has("user")) {
                JsonNode user = likeData.get("user");
                parsed.put("userId", user.has("userId") ? user.get("userId").asText() : "");
                parsed.put("userName", user.has("uniqueId") ? user.get("uniqueId").asText() : "");
            }
            
            parsed.put("likeCount", likeData.has("likeCount") ? likeData.get("likeCount").asInt() : 1);
            parsed.put("totalLikeCount", likeData.has("totalLikeCount") ? likeData.get("totalLikeCount").asInt() : 0);
            
        } catch (Exception e) {
            log.error("Failed to parse like data", e);
        }
        
        return parsed;
    }
    
    /**
     * Parse room user stats (viewer count, etc.)
     */
    public Map<String, Object> parseRoomUserData(JsonNode roomUserData) {
        Map<String, Object> parsed = new HashMap<>();
        
        try {
            parsed.put("viewerCount", roomUserData.has("viewerCount") ? roomUserData.get("viewerCount").asInt() : 0);
            
            if (roomUserData.has("topViewers") && roomUserData.get("topViewers").isArray()) {
                parsed.put("topViewers", roomUserData.get("topViewers"));
            }
            
        } catch (Exception e) {
            log.error("Failed to parse room user data", e);
        }
        
        return parsed;
    }
}
