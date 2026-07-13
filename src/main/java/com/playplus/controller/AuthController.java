package com.playplus.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.playplus.config.JwtUtil;
import com.playplus.dto.JwtResponse;
import com.playplus.dto.LoginRequest;
import com.playplus.dto.SignupRequest;
import com.playplus.model.User;
import com.playplus.service.UserService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userService.findByUsername(request.getUsername());
        
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid password");
        }
        
        String token = jwtUtil.generateToken(user.getUsername());
        return ResponseEntity.ok(new JwtResponse(token, user.getId(), user.getUsername(), user.getEmail(),user.getProfileImage()));
    }
    
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username is already taken");
        }
        
        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email is already in use");
        }
        
        userService.registerUser(
            request.getUsername(),
            request.getEmail(),
            request.getPassword(),
            request.getFullName()
        );
        
        return ResponseEntity.ok("User registered successfully");
    }
    
    // Forgot Password - Request verification code
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        System.out.println("📧 Forgot password request for: " + email);
        
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        
        try {
            User user = userService.findByEmail(email);
            if (user == null) {
                System.out.println("❌ Email not found: " + email);
                return ResponseEntity.badRequest().body("Email not found");
            }
            
            String code = userService.generateVerificationCode(email);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Verification code sent to your email");
            response.put("email", email);
            System.out.println("✅ Verification code sent to " + email);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to send verification code: " + e.getMessage());
        }
    }
    
    // Verify Code and Get Reset Token
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        
        System.out.println("🔑 Verifying code for: " + email);
        
        if (email == null || code == null) {
            return ResponseEntity.badRequest().body("Email and code are required");
        }
        
        String resetToken = userService.verifyCodeAndGenerateToken(email, code);
        if (resetToken == null) {
            System.out.println("❌ Invalid or expired code");
            return ResponseEntity.badRequest().body("Invalid or expired verification code");
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("resetToken", resetToken);
        response.put("message", "Verification successful");
        System.out.println("✅ Verification successful");
        return ResponseEntity.ok(response);
    }
    
    // Reset Password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String resetToken = request.get("resetToken");
        String newPassword = request.get("newPassword");
        
        System.out.println("🔑 Resetting password with token: " + resetToken);
        
        if (resetToken == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Reset token and new password are required");
        }
        
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body("Password must be at least 6 characters");
        }
        
        boolean success = userService.resetPassword(resetToken, newPassword);
        if (!success) {
            System.out.println("❌ Invalid or expired reset token");
            return ResponseEntity.badRequest().body("Invalid or expired reset token");
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset successfully");
        System.out.println("✅ Password reset successfully");
        return ResponseEntity.ok(response);
    }
    
    // Google Login - with clean username
    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> request) {
        String googleId = request.get("googleId");
        String email = request.get("email");
        String name = request.get("name");
        String profilePicture = request.get("profilePicture");
        
        System.out.println("🔑 Google login attempt for: " + email);
        System.out.println("🔑 Google ID: " + googleId);
        
        if (googleId == null || email == null) {
            return ResponseEntity.badRequest().body("Google ID and email are required");
        }
        
        try {
           User user = userService.findByGoogleId(googleId);

if (user == null) {

    user = userService.findByEmail(email);

    if (user != null) {

        user.setGoogleId(googleId);
        user.setIsGoogleUser(true);

        // Keep custom Play+ profile image
        if (user.getProfileImage() == null ||
            user.getProfileImage().contains("googleusercontent.com")) {

            user.setProfileImage(profilePicture);
        }

        user = userService.save(user);

        System.out.println("✅ Google account linked to existing user: " + email);

    } else {

        // Create new user with clean username
        user = new User();

        String cleanUsername = generateCleanUsername(email, name);

        user.setUsername(cleanUsername);
        user.setEmail(email);
        user.setFullName(name != null ? name : email.split("@")[0]);
        user.setGoogleId(googleId);
        user.setIsGoogleUser(true);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        user.setProfileImage(
            profilePicture != null
                ? profilePicture
                : "https://ui-avatars.com/api/?background=3ea6ff&color=fff&name=" +
                  (name != null && !name.isEmpty() ? name.charAt(0) : email.charAt(0))
        );

        user = userService.save(user);

        System.out.println("✅ New user created via Google: " + email);
        System.out.println("✅ Username: " + cleanUsername);
    }

} else {

    // Existing Google user logging in again
    if (user.getProfileImage() == null ||
        user.getProfileImage().contains("googleusercontent.com")) {

        user.setProfileImage(profilePicture);
    }

    user = userService.save(user);
}
            String token = jwtUtil.generateToken(user.getUsername());
            JwtResponse response = new JwtResponse(token, user.getId(), user.getUsername(), user.getEmail(),user.getProfileImage());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ Google login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Google login failed: " + e.getMessage());
        }
    }
    
    // Helper method to generate clean username
    private String generateCleanUsername(String email, String name) {
        String baseUsername;
        
        // Try to use name first
        if (name != null && !name.trim().isEmpty()) {
            // Remove spaces and special characters
            baseUsername = name.toLowerCase()
                .replaceAll("[^a-zA-Z0-9]", "")
                .replaceAll("\\s+", "");
        } else {
            // Use email prefix
            baseUsername = email.split("@")[0]
                .toLowerCase()
                .replaceAll("[^a-zA-Z0-9]", "");
        }
        
        // Check if username already exists
        String finalUsername = baseUsername;
        int counter = 0;
        while (userService.existsByUsername(finalUsername)) {
            counter++;
            finalUsername = baseUsername + counter;
        }
        
        // If username is too short or empty, use a default
        if (finalUsername.length() < 3) {
            finalUsername = "user" + System.currentTimeMillis() % 10000;
        }
        
        return finalUsername;
    }
}