package com.ayush.leaderboardproject.Controllers;

import com.ayush.leaderboardproject.Security.JwtUtil;
import com.ayush.leaderboardproject.Service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final JwtUtil jwtUtil;

    public ProfileController(ProfileService profileService, JwtUtil jwtUtil) {
        this.profileService = profileService;
        this.jwtUtil = jwtUtil;
    }

    private String getUsernameFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = authHeader.substring(7);
        return jwtUtil.extractUsername(token);
    }

    @GetMapping
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
            String username = getUsernameFromRequest(request);
            Map<String, Object> profile = profileService.getProfile(username);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(HttpServletRequest request,
                                           @RequestBody Map<String, String> body) {
        try {
            String username = getUsernameFromRequest(request);
            profileService.updateProfile(username, body.get("email"), body.get("password"));
            return ResponseEntity.ok("Profile updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/picture")
    public ResponseEntity<?> uploadPicture(HttpServletRequest request,
                                           @RequestParam("file") MultipartFile file) {
        try {
            String username = getUsernameFromRequest(request);
            String url = profileService.uploadProfilePicture(username, file);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}