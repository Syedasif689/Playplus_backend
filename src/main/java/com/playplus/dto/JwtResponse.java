package com.playplus.dto;

public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String profileImage;
    private String username;
    private String email;
    
    public JwtResponse(String token, Long id, String username, String email, String profileImage) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.profileImage = profileImage;
    }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}