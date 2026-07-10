package com.ayush.leaderboardproject.Controllers;

import com.ayush.leaderboardproject.Service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String,String> request){
        try{
            String token =authService.register(
                    request.get("username"),
                    request.get("email"),
                    request.get("password")
            );
            return ResponseEntity.ok(Map.of("token",token));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> request){
        try{
            String token = authService.login(
                    request.get("username"),
                    request.get("password")
            );
            return ResponseEntity.ok(Map.of("token",token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("oauth-register")
    public ResponseEntity<?> oauthRegister(@RequestBody Map<String,String> request){
        try{
            String token = authService.oauthRegister(
                    request.get("username"),
                    request.get("email")
        );
            return ResponseEntity.ok(Map.of("token",token));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
