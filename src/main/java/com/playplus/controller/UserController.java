package com.playplus.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.playplus.config.JwtUtil;
import com.playplus.model.Subscription;
import com.playplus.model.User;
import com.playplus.model.Video;
import com.playplus.model.VideoHistory;
import com.playplus.repository.VideoHistoryRepository;
import com.playplus.repository.VideoRepository;
import com.playplus.service.ChannelService;
import com.playplus.service.UserService;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UserController {

    @Autowired
    private ChannelService channelService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private VideoHistoryRepository videoHistoryRepository;

    @Autowired
    private VideoRepository videoRepository;

    /**
     * Get all channels the authenticated user is subscribed to.
     * GET /api/user/subscriptions
     */
    @GetMapping("/subscriptions")
    public ResponseEntity<?> getUserSubscriptions(@RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = getUserIdFromAuthHeader(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            List<Subscription> subscriptions = channelService.getUserSubscriptions(userId);
            List<String> channelNames = subscriptions.stream()
                    .map(s -> s.getChannel().getUsername())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(channelNames);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch subscriptions: " + e.getMessage()));
        }
    }

    /**
     * Get current user's watch history
     * GET /api/user/history
     */
    @GetMapping("/history")
    public ResponseEntity<?> getWatchHistory(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
        }
        List<VideoHistory> history = videoHistoryRepository.findByUserIdOrderByWatchedAtDesc(userId);
        // Return video details for each history entry
        List<Map<String, Object>> response = history.stream().map(h -> {
            Video v = h.getVideo();
            Map<String, Object> map = new HashMap<>();
            map.put("id", v.getId());
            map.put("title", v.getTitle());
            map.put("thumbnail", v.getThumbnail());
            map.put("creator", v.getCreator());
            map.put("views", v.getViews());
            map.put("likes", v.getLikes());
            map.put("watchedAt", h.getWatchedAt());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
     /**
 * Remove a single video from watch history
 * DELETE /api/user/history/{videoId}
 */
    @Transactional
    @DeleteMapping("/history/{videoId}")
        public ResponseEntity<?> removeHistoryItem(
        @RequestHeader("Authorization") String authHeader,
        @PathVariable Long videoId) {

        Long userId = getUserIdFromAuthHeader(authHeader);

        if (userId == null) {
            return ResponseEntity.status(401)
                .body(Map.of("error", "User not authenticated"));
        }

        videoHistoryRepository.deleteByUserIdAndVideoId(userId, videoId);

          return ResponseEntity.ok(Map.of("message", "Video removed from history"));
    }

    /**
     * Clear entire watch history
     * DELETE /api/user/history
     */
    @DeleteMapping("/history")
    public ResponseEntity<?> clearWatchHistory(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
        }
        videoHistoryRepository.deleteAllByUserId(userId);
        return ResponseEntity.ok(Map.of("message", "History cleared"));
    }

    /**
     * Get liked videos (list of Video objects)
     * GET /api/user/liked-videos
     */
    @GetMapping("/liked-videos")
    public ResponseEntity<?> getLikedVideos(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
        }
        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }
       List<Long> likedVideoIds = new ArrayList<>(user.getLikedVideos());
        if (likedVideoIds == null || likedVideoIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<Video> likedVideos = videoRepository.findAllById(likedVideoIds);
        // Preserve order from likedVideoIds list
        Map<Long, Video> videoMap = likedVideos.stream().collect(Collectors.toMap(Video::getId, v -> v));
        List<Video> ordered = likedVideoIds.stream()
                .filter(videoMap::containsKey)
                .map(videoMap::get)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ordered);
    }

    /**
     * Helper method to extract user ID from JWT token.
     */
    private Long getUserIdFromAuthHeader(String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtUtil.extractUsername(token);
                if (username != null) {
                    User user = userService.findByUsername(username);
                    if (user != null) {
                        return user.getId();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error extracting user ID from token: " + e.getMessage());
        }
        return null;
    }
}