package com.playplus.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    private String fullName;
    
    private String profilePicture;
    
    @Column(length = 500)
    private String bio;
    
    private Integer subscriberCount = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // NEW: Store liked video IDs
    @ElementCollection
    @CollectionTable(name = "user_liked_videos", 
        joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "video_id")
    private List<Long> likedVideos = new ArrayList<>();
    
    // NEW: Store disliked video IDs
    @ElementCollection
    @CollectionTable(name = "user_disliked_videos", 
        joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "video_id")
    private List<Long> dislikedVideos = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (profilePicture == null) {
            profilePicture = "https://ui-avatars.com/api/?background=3ea6ff&color=fff&name=" + username;
        }
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
    
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    
    public Integer getSubscriberCount() { return subscriberCount; }
    public void setSubscriberCount(Integer subscriberCount) { this.subscriberCount = subscriberCount; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public List<Long> getLikedVideos() { return likedVideos; }
    public void setLikedVideos(List<Long> likedVideos) { this.likedVideos = likedVideos; }
    
    public List<Long> getDislikedVideos() { return dislikedVideos; }
    public void setDislikedVideos(List<Long> dislikedVideos) { this.dislikedVideos = dislikedVideos; }
}