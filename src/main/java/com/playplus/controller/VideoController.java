package com.playplus.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import com.playplus.model.Comment;
import com.playplus.model.User;
import com.playplus.model.Video;
import com.playplus.model.VideoHistory;
import com.playplus.repository.CommentRepository;
import com.playplus.repository.VideoHistoryRepository;
import com.playplus.repository.VideoRepository;
import com.playplus.service.UserService;

import jakarta.transaction.Transactional;

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
    private CommentRepository commentRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private VideoHistoryRepository videoHistoryRepository;

    // ==================== SPECIFIC PATHS FIRST ====================

    @GetMapping("/all")
    public ResponseEntity<?> getAllVideos() {
        List<Video> videos = videoRepository.findAll();
        return ResponseEntity.ok(videos);
    }
    
    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<?> getVideosByCreator(@PathVariable Long creatorId) {
        List<Video> videos = videoRepository.findByCreatorId(creatorId);
        return ResponseEntity.ok(videos);
    }
    
    // ==================== DYNAMIC PATHS ====================

    @PostMapping("/{videoId}/view")
    public ResponseEntity<?> trackView(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long videoId) {
        
        try {
            System.out.println("👁️ View tracking request for video: " + videoId);
            
            Video video = videoRepository.findById(videoId).orElse(null);
            if (video == null) {
                System.out.println("❌ Video not found: " + videoId);
                return ResponseEntity.notFound().build();
            }
            
            Long userId = null;
            boolean alreadyViewed = false;
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    userId = getUserIdFromAuthHeader(authHeader);
                    System.out.println("👤 User ID from token: " + userId);
                } catch (Exception e) {
                    System.out.println("⚠️ User not authenticated, treating as guest");
                }
            }
            
            if (video.getViewers() == null) {
                video.setViewers(new ArrayList<>());
            }
            
            if (userId != null) {
                alreadyViewed = video.getViewers().contains(userId);
                if (!alreadyViewed) {
                    video.getViewers().add(userId);
                    video.setViews(video.getViews() + 1);
                    videoRepository.save(video);
                    System.out.println("✅ View tracked for user: " + userId);
                    System.out.println("📊 Total views: " + video.getViews());
                } else {
                    System.out.println("⏭️ User " + userId + " already viewed video: " + videoId);
                }
            } else {
                video.setViews(video.getViews() + 1);
                videoRepository.save(video);
                System.out.println("✅ View tracked for guest");
                System.out.println("📊 Total views: " + video.getViews());
            }

            // ========== SAVE TO HISTORY (only for authenticated users) ==========
            if (userId != null) {
                try {
                    Optional<VideoHistory> existing = videoHistoryRepository.findByUserIdAndVideoId(userId, videoId);
                    if (existing.isPresent()) {
                        VideoHistory history = existing.get();
                        history.setWatchedAt(LocalDateTime.now());
                        videoHistoryRepository.save(history);
                    } else {
                        VideoHistory history = new VideoHistory();
                        User user = userService.findById(userId);
                        if (user != null) {
                            history.setUser(user);
                            history.setVideo(video);
                            history.setWatchedAt(LocalDateTime.now());
                            videoHistoryRepository.save(history);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Failed to save history: " + e.getMessage());
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("views", video.getViews());
            response.put("viewed", !alreadyViewed);
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ Error tracking view: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error tracking view: " + e.getMessage());
        }
    }
    
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
    
   @PostMapping("/{videoId}/like")
public ResponseEntity<?> likeVideo(
        @RequestHeader("Authorization") String authHeader,
        @PathVariable Long videoId) {

    try {

        Long userId = getUserIdFromAuthHeader(authHeader);

        System.out.println("========== LIKE DEBUG ==========");
        System.out.println("UserId = " + userId);
        System.out.println("VideoId = " + videoId);

        System.out.println("STEP 1");

        User user = userService.findById(userId);

        System.out.println("User = " + user);

        System.out.println("STEP 2");

        Video video = videoRepository.findById(videoId).orElse(null);

        System.out.println("Video = " + video);

        System.out.println("STEP 3");

        Video result = userService.likeVideo(userId, videoId);

        System.out.println("STEP 4");

        System.out.println("Result = " + result);
        System.out.println("================================");

        return ResponseEntity.ok(result);

    } catch (Exception e) {

        System.out.println("❌ LIKE API FAILED");
        System.out.println("Exception Class: " + e.getClass().getName());
        System.out.println("Exception Message: " + e.getMessage());

        e.printStackTrace(System.out);

        return ResponseEntity.internalServerError()
                .body("ERROR: " + e.getClass().getName()
                        + " : " + e.getMessage());
    }
}
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
            video.setAllowDownload(videoRequest.getAllowDownload() != null && videoRequest.getAllowDownload());
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
            if (videoRequest.getAllowDownload() != null) {
                existingVideo.setAllowDownload(videoRequest.getAllowDownload());
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
    
    @Transactional
    @DeleteMapping("/{videoId}")
    public ResponseEntity<?> deleteVideo(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
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
            
            List<Comment> comments = commentRepository.findByVideoId(videoId);
            commentRepository.deleteAll(comments);
            videoRepository.delete(video);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Video deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting video: " + e.getMessage());
        }
    }
    
    // ✅ CRITICAL FIX: Changed path to /video/{videoId}
    @GetMapping("/video/{videoId}")
    public ResponseEntity<?> getVideo(@PathVariable Long videoId) {
        Video video = videoRepository.findById(videoId).orElse(null);
        if (video == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(video);
    }
    
    // Helper method
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