package com.playplus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.playplus.model.User;
import com.playplus.model.Video;
import com.playplus.repository.UserRepository;
import com.playplus.repository.VideoRepository;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VideoRepository videoRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User registerUser(String username, String email, String password, String fullName) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        
        return userRepository.save(user);
    }
    
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
    
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    @Transactional
    public Video likeVideo(Long userId, Long videoId) {
        User user = findById(userId);
        Video video = videoRepository.findById(videoId).orElse(null);
        
        if (user == null || video == null) return null;
        
        if (user.getDislikedVideos().contains(videoId)) {
            user.getDislikedVideos().remove(videoId);
            video.setDislikes(video.getDislikes() - 1);
        }
        
        if (user.getLikedVideos().contains(videoId)) {
            user.getLikedVideos().remove(videoId);
            video.setLikes(video.getLikes() - 1);
        } else {
            user.getLikedVideos().add(videoId);
            video.setLikes(video.getLikes() + 1);
        }
        
        userRepository.save(user);
        return videoRepository.save(video);
    }
    
    @Transactional
    public Video dislikeVideo(Long userId, Long videoId) {
        User user = findById(userId);
        Video video = videoRepository.findById(videoId).orElse(null);
        
        if (user == null || video == null) return null;
        
        if (user.getLikedVideos().contains(videoId)) {
            user.getLikedVideos().remove(videoId);
            video.setLikes(video.getLikes() - 1);
        }
        
        if (user.getDislikedVideos().contains(videoId)) {
            user.getDislikedVideos().remove(videoId);
            video.setDislikes(video.getDislikes() - 1);
        } else {
            user.getDislikedVideos().add(videoId);
            video.setDislikes(video.getDislikes() + 1);
        }
        
        userRepository.save(user);
        return videoRepository.save(video);
    }
    
    public String getVideoReaction(Long userId, Long videoId) {
        User user = findById(userId);
        if (user == null) return "none";
        
        if (user.getLikedVideos().contains(videoId)) return "like";
        if (user.getDislikedVideos().contains(videoId)) return "dislike";
        return "none";
    }
}