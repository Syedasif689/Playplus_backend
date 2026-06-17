package com.playplus.controller;

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
@CrossOrigin(
    origins = "http://localhost:3000",
    allowedHeaders = "*",
    allowCredentials = "true"
)

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
        
        return ResponseEntity.ok(new JwtResponse(token, user.getId(), user.getUsername(), user.getEmail()));
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
}