package com.playplus.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.playplus.config.JwtUtil;
import com.playplus.model.User;
import com.playplus.model.Video;
import com.playplus.repository.VideoRepository;
import com.playplus.service.UserService;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(
    origins = "http://localhost:3000",
    allowedHeaders = "*",
    allowCredentials = "true"
)

public class VideoController {
    
    @Autowired
    private VideoRepository videoRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // IMPORTANT: Specific paths FIRST, dynamic paths LAST
    
    // 1. Get all videos
    @GetMapping("/all")
    public ResponseEntity<?> getAllVideos() {
        List<Video> videos = videoRepository.findAll();
        return ResponseEntity.ok(videos);
    }
    
    // 2. Get videos by creator
    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<?> getVideosByCreator(@PathVariable Long creatorId) {
        List<Video> videos = videoRepository.findByCreatorId(creatorId);
        return ResponseEntity.ok(videos);
    }
    
    // 3. Get video reaction status
    @GetMapping("/{videoId}/reaction")
    public ResponseEntity<?> getReaction(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long videoId) {
        
        Long userId = getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        
        String reaction = userService.getVideoReaction(userId, videoId);
        Map<String, String> response = new HashMap<>();
        response.put("reaction", reaction);
        
        return ResponseEntity.ok(response);
    }
    
    // 4. Like a video
    @PostMapping("/{videoId}/like")
    public ResponseEntity<?> likeVideo(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long videoId) {
        
        Long userId = getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        
        Video video = userService.likeVideo(userId, videoId);
        if (video == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("likes", video.getLikes());
        response.put("dislikes", video.getDislikes());
        response.put("reaction", "like");
        
        return ResponseEntity.ok(response);
    }
    
    // 5. Dislike a video
    @PostMapping("/{videoId}/dislike")
    public ResponseEntity<?> dislikeVideo(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long videoId) {
        
        Long userId = getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        
        Video video = userService.dislikeVideo(userId, videoId);
        if (video == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("likes", video.getLikes());
        response.put("dislikes", video.getDislikes());
        response.put("reaction", "dislike");
        
        return ResponseEntity.ok(response);
    }
    
    // 6. Upload a video
    @PostMapping("/upload")
    public ResponseEntity<?> uploadVideo(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Video videoRequest) {
        
        try {
            Long userId = getUserIdFromAuthHeader(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body("User not authenticated");
            }
            
            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.status(404).body("User not found");
            }
            
            Video video = new Video();
            video.setTitle(videoRequest.getTitle());
            video.setDescription(videoRequest.getDescription());
            video.setVideoUrl(videoRequest.getVideoUrl());
            video.setThumbnail(videoRequest.getThumbnail());
            video.setCreator(user.getUsername());
            video.setCreatorId(userId);
            video.setViews(0);
            video.setLikes(0);
            video.setDislikes(0);
            video.setShareCount(0);
            
            Video savedVideo = videoRepository.save(video);
            return ResponseEntity.ok(savedVideo);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error uploading video: " + e.getMessage());
        }
    }
    
    // 7. Update a video
    @PutMapping("/{videoId}")
    public ResponseEntity<?> updateVideo(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long videoId,
            @RequestBody Video videoRequest) {
        
        try {
            Long userId = getUserIdFromAuthHeader(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body("User not authenticated");
            }
            
            Video existingVideo = videoRepository.findById(videoId).orElse(null);
            if (existingVideo == null) {
                return ResponseEntity.notFound().build();
            }
            
            if (!existingVideo.getCreatorId().equals(userId)) {
                return ResponseEntity.status(403).body("You don't have permission to edit this video");
            }
            
            if (videoRequest.getTitle() != null) {
                existingVideo.setTitle(videoRequest.getTitle());
            }
            if (videoRequest.getDescription() != null) {
                existingVideo.setDescription(videoRequest.getDescription());
            }
            if (videoRequest.getVideoUrl() != null) {
                existingVideo.setVideoUrl(videoRequest.getVideoUrl());
            }
            if (videoRequest.getThumbnail() != null) {
                existingVideo.setThumbnail(videoRequest.getThumbnail());
            }
            
            Video updatedVideo = videoRepository.save(existingVideo);
            return ResponseEntity.ok(updatedVideo);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating video: " + e.getMessage());
        }
    }
    
    // 8. Delete a video
    @DeleteMapping("/{videoId}")
    public ResponseEntity<?> deleteVideo(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long videoId) {
        
        try {
            Long userId = getUserIdFromAuthHeader(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body("User not authenticated");
            }
            
            Video video = videoRepository.findById(videoId).orElse(null);
            if (video == null) {
                return ResponseEntity.notFound().build();
            }
            
            if (!video.getCreatorId().equals(userId)) {
                return ResponseEntity.status(403).body("You don't have permission to delete this video");
            }
            
            videoRepository.delete(video);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Video deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting video: " + e.getMessage());
        }
    }
    
    // 9. Get video by ID - MUST BE LAST
    @GetMapping("/{videoId}")
    public ResponseEntity<?> getVideo(@PathVariable Long videoId) {
        Video video = videoRepository.findById(videoId).orElse(null);
        if (video == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(video);
    }
    
    // Helper method to get userId from JWT
    private Long getUserIdFromAuthHeader(String authHeader) {
        try {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            if (username != null) {
                User user = userService.findByUsername(username);
                if (user != null) {
                    return user.getId();
                }
            }
        } catch (Exception e) {
            System.out.println("Error extracting user ID: " + e.getMessage());
        }
        return null;
    }
}