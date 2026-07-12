package com.playplus.dto;

import java.util.List;

public class ProfileResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String bio;
    private String profileImage;
    private List<SocialLinkRequest> socialLinks;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}