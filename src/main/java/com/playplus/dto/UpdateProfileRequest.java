package com.playplus.dto;

import java.util.List;

public class UpdateProfileRequest {

    private String username;
    private String bio;
    private List<SocialLinkRequest> socialLinks;

    public UpdateProfileRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<SocialLinkRequest> getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(List<SocialLinkRequest> socialLinks) {
        this.socialLinks = socialLinks;
    }
}