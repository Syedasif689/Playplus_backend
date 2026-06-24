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
@Table(name = "videos")
public class Video {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 2000)
    private String description;
    
    @Column(nullable = false)
    private String videoUrl;
    
    private String thumbnail;
    
    private String creator;
    
    private Long creatorId;
    
    private Integer views = 0;
    
    private Integer likes = 0;
    
    private Integer dislikes = 0;
    
    private Integer shareCount = 0;
    
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
    
    // ✅ NEW: Track unique viewers for view counting
    @ElementCollection
    @CollectionTable(
        name = "video_viewers", 
        joinColumns = @JoinColumn(name = "video_id")
    )
    @Column(name = "user_id")
    private List<Long> viewers = new ArrayList<>();
    
    @Column(name = "allow_download", nullable = false)
    private Boolean allowDownload = false;
    
    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        if (thumbnail == null || thumbnail.isEmpty()) {
            thumbnail = "https://img.youtube.com/vi/default/mqdefault.jpg";
        }
    }
    
    // Getters and Setters
    public Boolean getAllowDownload() { return allowDownload; }
    public void setAllowDownload(Boolean allowDownload) { this.allowDownload = allowDownload; }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    
    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
    
    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    
    public Integer getViews() { return views; }
    public void setViews(Integer views) { this.views = views; }
    
    public Integer getLikes() { return likes; }
    public void setLikes(Integer likes) { this.likes = likes; }
    
    public Integer getDislikes() { return dislikes; }
    public void setDislikes(Integer dislikes) { this.dislikes = dislikes; }
    
    public Integer getShareCount() { return shareCount; }
    public void setShareCount(Integer shareCount) { this.shareCount = shareCount; }
    
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    
    // ✅ NEW: Getters and Setters for viewers
    public List<Long> getViewers() { return viewers; }
    public void setViewers(List<Long> viewers) { this.viewers = viewers; }
}