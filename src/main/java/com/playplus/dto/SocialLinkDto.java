package com.playplus.dto;

public class SocialLinkDto {

    private String platform;
    private String url;

    public SocialLinkDto() {
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