package com.playplus.dto;

public class SocialLinkRequest {

    private String platform;
    private String url;

    public SocialLinkRequest() {
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}