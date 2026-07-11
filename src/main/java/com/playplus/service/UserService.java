package com.playplus.service;

import java.time.LocalDateTime;
import java.util.UUID;

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
    
    @Autowired
    private EmailService emailService;
    
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
    
    // ✅ ADD THIS - Find by Email
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    
    // ✅ ADD THIS - Find by Google ID
    public User findByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId).orElse(null);
    }
    
    // ✅ ADD THIS - Save user
    public User save(User user) {
        return userRepository.save(user);
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
    
    // Google OAuth2 user creation
    public User createOrUpdateGoogleUser(String googleId, String email, String name, String profilePicture) {
        User existingUser = userRepository.findByGoogleId(googleId).orElse(null);
        
        if (existingUser != null) {
            return existingUser;
        }
        
        User userWithEmail = userRepository.findByEmail(email).orElse(null);
        if (userWithEmail != null) {
            userWithEmail.setGoogleId(googleId);
            userWithEmail.setIsGoogleUser(true);
            return userRepository.save(userWithEmail);
        }
        
        User newUser = new User();
        newUser.setUsername(email.split("@")[0] + System.currentTimeMillis());
        newUser.setEmail(email);
        newUser.setFullName(name);
        newUser.setGoogleId(googleId);
        newUser.setIsGoogleUser(true);
        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        newUser.setProfileImage(profilePicture);
        
        return userRepository.save(newUser);
    }
    
    // Generate verification code for password reset
    @Transactional
    public String generateVerificationCode(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return null;
        }
        
        String code = String.format("%06d", (int)(Math.random() * 1000000));
        user.setVerificationCode(code);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);
        
        emailService.sendVerificationCode(email, code);
        
        return code;
    }
    
    // Verify the code and generate reset token
    @Transactional
    public String verifyCodeAndGenerateToken(String email, String code) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return null;
        }
        
        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(code)) {
            return null;
        }
        
        if (user.getVerificationCodeExpiry() == null || 
            user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
            return null;
        }
        
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);
        
        return resetToken;
    }
    
    // Reset password
    @Transactional
public boolean resetPassword(String resetToken, String newPassword) {

    User user = userRepository.findByResetToken(resetToken).orElse(null);

    if (user == null) {
        return false;
    }

    if (user.getResetTokenExpiry() == null ||
        user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
        return false;
    }

    user.setPassword(passwordEncoder.encode(newPassword));
    user.setResetToken(null);
    user.setResetTokenExpiry(null);
    user.setVerificationCode(null);
    user.setVerificationCodeExpiry(null);

    userRepository.save(user);

    // REMOVE THIS LINE
    // emailService.sendPasswordResetConfirmation(user.getEmail());

    return true;
}
    // Video Like methods
    @Transactional
public Video likeVideo(Long userId, Long videoId) {

    User user = findById(userId);
    Video video = videoRepository.findById(videoId).orElse(null);

    if (user == null || video == null) return null;

    // Remove dislike if present
    if (user.getDislikedVideos().contains(videoId)) {
        user.getDislikedVideos().remove(videoId);
        video.setDislikes(Math.max(0, video.getDislikes() - 1));
    }

    // Toggle like
    if (user.getLikedVideos().contains(videoId)) {
        user.getLikedVideos().remove(videoId);
        video.setLikes(Math.max(0, video.getLikes() - 1));
    } else {
        // Prevent duplicate insert
        if (!user.getLikedVideos().contains(videoId)) {
            user.getLikedVideos().add(videoId);
            video.setLikes(video.getLikes() + 1);
        }
    }

    userRepository.save(user);
    return videoRepository.save(video);
}
    
   @Transactional
public Video dislikeVideo(Long userId, Long videoId) {

    User user = findById(userId);
    Video video = videoRepository.findById(videoId).orElse(null);

    if (user == null || video == null) return null;

    // Remove like if present
    if (user.getLikedVideos().contains(videoId)) {
        user.getLikedVideos().remove(videoId);
        video.setLikes(Math.max(0, video.getLikes() - 1));
    }

    // Toggle dislike
   if (user.getDislikedVideos().contains(videoId)) {

    user.getDislikedVideos().remove(videoId);
    video.setDislikes(Math.max(0, video.getDislikes() - 1));

} else {

    System.out.println("Before = " + user.getDislikedVideos());

    boolean added = user.getDislikedVideos().add(videoId);

    System.out.println("Added = " + added);

    System.out.println("After = " + user.getDislikedVideos());

    if (added) {
        video.setDislikes(video.getDislikes() + 1);
    }
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