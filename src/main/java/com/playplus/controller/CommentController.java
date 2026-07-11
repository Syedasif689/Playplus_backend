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
import com.playplus.model.Comment;
import com.playplus.model.User;
import com.playplus.service.CommentService;
import com.playplus.service.UserService;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(
    origins = "http://localhost:3000",
    allowedHeaders = "*",
    allowCredentials = "true"
)
public class CommentController {
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // Get comments for a video
    @GetMapping("/video/{videoId}")
    public ResponseEntity<?> getComments(@PathVariable Long videoId) {
        List<Comment> comments = commentService.getCommentsByVideoId(videoId);
        return ResponseEntity.ok(comments);
    }
     @PutMapping("/profile-image")
       public ResponseEntity<?> updateProfileImage(
        @RequestHeader("Authorization") String authHeader,
        @RequestBody Map<String, String> request) {

    try {

        Long userId = getUserIdFromAuthHeader(authHeader);

        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "User not authenticated"));
        }

        User user = userService.findById(userId);

        if (user == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "User not found"));
        }

        user.setProfileImage(request.get("profileImage"));

        userService.save(user);

        return ResponseEntity.ok(user);

    } catch (Exception e) {

        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));

    }
}
    // Add a comment
    @PostMapping("/video/{videoId}")
    public ResponseEntity<?> addComment(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long videoId,
            @RequestBody Map<String, Object> request) {
        
        Long userId = getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        
        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }
        
        String text = (String) request.get("text");
        Long parentCommentId = request.get("parentCommentId") != null ? 
            Long.valueOf(request.get("parentCommentId").toString()) : null;
        
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Comment text is required");
        }
        
        Comment comment = commentService.addComment(videoId, userId, user.getUsername(), text, parentCommentId);
        return ResponseEntity.ok(comment);
    }
    
    // Update a comment
    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long commentId,
            @RequestBody Map<String, String> request) {
        
        Long userId = getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        
        String newText = request.get("text");
        if (newText == null || newText.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Comment text is required");
        }
        
        Comment updatedComment = commentService.updateComment(commentId, userId, newText);
        if (updatedComment == null) {
            return ResponseEntity.status(403).body("You don't have permission to edit this comment");
        }
        
        return ResponseEntity.ok(updatedComment);
    }
    
    // Delete a comment
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long commentId) {
        
        Long userId = getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        
        boolean deleted = commentService.deleteComment(commentId, userId);
        if (!deleted) {
            return ResponseEntity.status(403).body("You don't have permission to delete this comment");
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Comment deleted successfully");
        return ResponseEntity.ok(response);
    }
    
    // Like/Unlike a comment
    @PostMapping("/{commentId}/like")
    public ResponseEntity<?> likeComment(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long commentId) {
        
        Long userId = getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        
        int likes = commentService.likeComment(commentId, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("likes", likes);
        response.put("liked", commentService.hasUserLikedComment(commentId, userId));
        
        return ResponseEntity.ok(response);
    }
    
    // Get like status for a comment
    @GetMapping("/{commentId}/like-status")
    public ResponseEntity<?> getLikeStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long commentId) {
        
        Long userId = getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        
        boolean liked = commentService.hasUserLikedComment(commentId, userId);
        Map<String, Object> response = new HashMap<>();
        response.put("liked", liked);
        
        return ResponseEntity.ok(response);
    }
    
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