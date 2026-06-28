package com.playplus.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.playplus.config.JwtUtil;
import com.playplus.dto.ChannelResponse;
import com.playplus.dto.SubscriptionResponse;
import com.playplus.model.User;
import com.playplus.service.ChannelService;
import com.playplus.service.UserService;

@RestController
@RequestMapping("/api/channels")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ChannelController {

    @Autowired
    private ChannelService channelService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/{username}")
    public ResponseEntity<?> getChannel(
            @PathVariable String username,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long currentUserId = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String tokenUsername = jwtUtil.extractUsername(token);
                if (tokenUsername != null) {
                    User user = userService.findByUsername(tokenUsername);
                    if (user != null) {
                        currentUserId = user.getId();
                    }
                }
            }
            ChannelResponse response = channelService.getChannelInfo(username, currentUserId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @PostMapping("/{username}/subscribe")
    public ResponseEntity<?> subscribe(
            @PathVariable String username,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = getUserIdFromAuthHeader(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            SubscriptionResponse response = channelService.subscribeToChannel(userId, username);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{username}/subscribe")
    public ResponseEntity<?> unsubscribe(
            @PathVariable String username,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = getUserIdFromAuthHeader(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            SubscriptionResponse response = channelService.unsubscribeFromChannel(userId, username);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping("/{username}/subscribed")
    public ResponseEntity<?> getSubscriptionStatus(
            @PathVariable String username,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = getUserIdFromAuthHeader(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            User channelOwner = channelService.getChannelOwner(username);
            boolean isSubscribed = channelService.isUserSubscribed(userId, channelOwner.getId());
            Long subscriberCount = channelService.getSubscriberCount(channelOwner.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("isSubscribed", isSubscribed);
            response.put("subscriberCount", subscriberCount);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

   private Long getUserIdFromAuthHeader(String authHeader) {
    try {

        System.out.println("AUTH HEADER = " + authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            System.out.println("TOKEN = " + token);

            String username = jwtUtil.extractUsername(token);

            System.out.println("USERNAME FROM TOKEN = " + username);

            if (username != null) {

                User user = userService.findByUsername(username);

                System.out.println("USER FROM DB = " + user);

                if (user != null) {
                    return user.getId();
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    return null;
}
}