package com.playplus.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.playplus.model.Comment;
import com.playplus.repository.CommentRepository;

@Service
public class CommentService {
    
    @Autowired
    private CommentRepository commentRepository;
    
    public List<Comment> getCommentsByVideoId(Long videoId) {
        return commentRepository.findByVideoIdOrderByCreatedAtDesc(videoId);
    }
    
    public Comment getCommentById(Long id) {
        return commentRepository.findById(id).orElse(null);
    }
    
    @Transactional
    public Comment addComment(Long videoId, Long userId, String username, String text, Long parentCommentId) {
        System.out.println("💾 Saving comment to database...");
        Comment comment = new Comment();
        comment.setVideoId(videoId);
        comment.setUserId(userId);
        comment.setUser(username);
        comment.setText(text);
        comment.setParentCommentId(parentCommentId);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setLikes(0);
        comment.setLikedBy(new java.util.ArrayList<>());
        
        Comment savedComment = commentRepository.save(comment);
        System.out.println("✅ Comment saved with ID: " + savedComment.getId());
        return savedComment;
    }
    
    @Transactional
    public Comment updateComment(Long commentId, Long userId, String newText) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null || !comment.getUserId().equals(userId)) {
            return null;
        }
        
        comment.setText(newText);
        comment.setUpdatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }
    
    @Transactional
    public boolean deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            System.out.println("❌ Comment not found: " + commentId);
            return false;
        }
        
        if (!comment.getUserId().equals(userId)) {
            System.out.println("❌ User " + userId + " doesn't own comment " + commentId);
            return false;
        }
        
        commentRepository.delete(comment);
        System.out.println("✅ Comment deleted: " + commentId);
        return true;
    }
    
    @Transactional
    public int likeComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            System.out.println("❌ Comment not found: " + commentId);
            return 0;
        }
        
        // Check if user already liked
        boolean alreadyLiked = comment.getLikedBy().contains(userId);
        
        if (alreadyLiked) {
            // Unlike - remove user from likedBy list
            comment.getLikedBy().remove(userId);
            comment.setLikes(comment.getLikes() - 1);
            System.out.println("👎 User " + userId + " unliked comment " + commentId);
        } else {
            // Like - add user to likedBy list
            comment.getLikedBy().add(userId);
            comment.setLikes(comment.getLikes() + 1);
            System.out.println("👍 User " + userId + " liked comment " + commentId);
        }
        
        Comment updatedComment = commentRepository.save(comment);
        return updatedComment.getLikes();
    }
    
    public boolean hasUserLikedComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            return false;
        }
        return comment.getLikedBy().contains(userId);
    }
}