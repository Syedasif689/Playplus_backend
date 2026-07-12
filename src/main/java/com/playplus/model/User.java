package com.playplus.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;


@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_image", length = 1000)
    private String profileImage;

    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    private String fullName;
    
    @Column(length = 500)
    private String bio;
    
    private Integer subscriberCount = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Google OAuth2 fields
    @Column(name = "google_id")
    private String googleId;
    
    @Column(name = "is_google_user")
    private Boolean isGoogleUser = false;
    
    // Forgot password fields
    @Column(name = "reset_token")
    private String resetToken;
    
    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;
    
    @Column(name = "verification_code")
    private String verificationCode;
    
    @Column(name = "verification_code_expiry")
    private LocalDateTime verificationCodeExpiry;
    
    // Store liked video IDs
   @ElementCollection
@CollectionTable(
    name = "user_liked_videos",
    joinColumns = @JoinColumn(name = "user_id")
)
@Column(name = "video_id")
private Set<Long> likedVideos = new HashSet<>();
    @OneToMany(
    mappedBy = "user",
    cascade = CascadeType.ALL,
    orphanRemoval = true
    )
    private List<SocialLink> socialLinks = new ArrayList<>();
    // Store disliked video IDs
    @ElementCollection
@CollectionTable(
    name = "user_disliked_videos",
    joinColumns = @JoinColumn(name = "user_id")
)
@Column(name = "video_id")
private Set<Long> dislikedVideos = new HashSet<>();
     
    // ✅ NEW: Channels this user is subscribed to (as subscriber)
    @OneToMany(mappedBy = "subscriber", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Subscription> subscribedChannels = new ArrayList<>();
    
    // ✅ NEW: Users who subscribed to this user's channel (as channel)
    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Subscription> subscribers = new ArrayList<>();
    
    @PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();

    if (profileImage == null || profileImage.isEmpty()) {
        profileImage =
            "https://ui-avatars.com/api/?background=3ea6ff&color=fff&name=" + username;
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
    
    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    
    public Integer getSubscriberCount() { return subscriberCount; }
    public void setSubscriberCount(Integer subscriberCount) { this.subscriberCount = subscriberCount; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }
    
    public Boolean getIsGoogleUser() { return isGoogleUser; }
    public void setIsGoogleUser(Boolean isGoogleUser) { this.isGoogleUser = isGoogleUser; }
    
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    
    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }
    
    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
    
    public LocalDateTime getVerificationCodeExpiry() { return verificationCodeExpiry; }
    public void setVerificationCodeExpiry(LocalDateTime verificationCodeExpiry) { this.verificationCodeExpiry = verificationCodeExpiry; }
    
    public Set<Long> getLikedVideos() { return likedVideos; }
    public void setLikedVideos(Set<Long> likedVideos) { this.likedVideos = likedVideos; }
    
    public Set<Long> getDislikedVideos() { return dislikedVideos; }
    public void setDislikedVideos(Set <Long> dislikedVideos) { this.dislikedVideos = dislikedVideos; }
    
    public List<SocialLink> getSocialLinks() {
    return socialLinks;
    }

    public void setSocialLinks(List<SocialLink> socialLinks) {
    this.socialLinks = socialLinks;
    }

public List<Subscription> getSubscribedChannels() {
    return subscribedChannels;
}

public void setSubscribedChannels(List<Subscription> subscribedChannels) {
    this.subscribedChannels = subscribedChannels;
}

public List<Subscription> getSubscribers() {
    return subscribers;
}

public void setSubscribers(List<Subscription> subscribers) {
    this.subscribers = subscribers;
}
    

  
}