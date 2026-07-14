package com.playplus.dto;
import java.util.List;
public class ChannelResponse {
    private Long id;
    private String username;
    private String fullName;
    private String bio;
    private String profileImage;
    private Integer subscriberCount;
    private Boolean isSubscribed;
    private Boolean isOwnChannel;
    private Integer videoCount;
    private List<SocialLinkRequest> socialLinks;
    // Default constructor
    public ChannelResponse() {}
    
    
    // All-args constructor
    public ChannelResponse(
        Long id,
        String username,
        String fullName,
        String bio,
        String profilePicture,
        Integer subscriberCount,
        Boolean isSubscribed,
        Boolean isOwnChannel,
        Integer videoCount,
        List<SocialLinkRequest> socialLinks
){
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.bio = bio;
        this.profileImage = profilePicture;
        this.subscriberCount = subscriberCount;
        this.isSubscribed = isSubscribed;
        this.isOwnChannel = isOwnChannel;
        this.videoCount = videoCount;
        this.socialLinks = socialLinks;
    }
    
    // Builder pattern for cleaner object creation
    public static class Builder {
        private Long id;
        private String username;
        private String fullName;
        private String bio;
        private String profileImage;
        private Integer subscriberCount;
        private Boolean isSubscribed;
        private Boolean isOwnChannel;
        private Integer videoCount;
        private List<SocialLinkRequest> socialLinks;
        public Builder id(Long id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder fullName(String fullName) { this.fullName = fullName; return this; }
        public Builder bio(String bio) { this.bio = bio; return this; }
        public Builder profileImage(String profileImage) { this.profileImage = profileImage; return this; }
        public Builder subscriberCount(Integer subscriberCount) { this.subscriberCount = subscriberCount; return this; }
        public Builder isSubscribed(Boolean isSubscribed) { this.isSubscribed = isSubscribed; return this; }
        public Builder isOwnChannel(Boolean isOwnChannel) { this.isOwnChannel = isOwnChannel; return this; }
        public Builder videoCount(Integer videoCount) { this.videoCount = videoCount; return this; }
        public Builder socialLinks(List<SocialLinkRequest> socialLinks) {
    this.socialLinks = socialLinks;
    return this;
}
        public ChannelResponse build() {
          return new ChannelResponse(
        id,
        username,
        fullName,
        bio,
        profileImage,
        subscriberCount,
        isSubscribed,
        isOwnChannel,
        videoCount,
        socialLinks
);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public String getProfileImage() {
        return profileImage;
    }
    
    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
    public List<SocialLinkRequest> getSocialLinks() {
    return socialLinks;
    }

    public void setSocialLinks(List<SocialLinkRequest> socialLinks) {
    this.socialLinks = socialLinks;
    }
    
    public Integer getSubscriberCount() {
        return subscriberCount;
    }
    
    public void setSubscriberCount(Integer subscriberCount) {
        this.subscriberCount = subscriberCount;
    }
    
    public Boolean getIsSubscribed() {
        return isSubscribed;
    }
    
    public void setIsSubscribed(Boolean isSubscribed) {
        this.isSubscribed = isSubscribed;
    }
    
    public Boolean getIsOwnChannel() {
        return isOwnChannel;
    }
    
    public void setIsOwnChannel(Boolean isOwnChannel) {
        this.isOwnChannel = isOwnChannel;
    }
    
    public Integer getVideoCount() {
        return videoCount;
    }
    
    public void setVideoCount(Integer videoCount) {
        this.videoCount = videoCount;
    }
}