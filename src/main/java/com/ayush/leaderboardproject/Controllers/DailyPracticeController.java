package com.ayush.leaderboardproject.Controllers;

import com.ayush.leaderboardproject.Security.JwtUtil;
import com.ayush.leaderboardproject.Service.DailyPracticeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/daily-practice")
public class DailyPracticeController {

    private final DailyPracticeService dailyPracticeService;
    private final JwtUtil jwtUtil;

    public DailyPracticeController(DailyPracticeService dailyPracticeService, JwtUtil jwtUtil) {
        this.dailyPracticeService = dailyPracticeService;
        this.jwtUtil = jwtUtil;
    }

    private String getUsername(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractUsername(token);
    }

    @GetMapping("/today")
    public ResponseEntity<?> getToday(HttpServletRequest request) {
        try {
            String username = getUsername(request);
            return ResponseEntity.ok(dailyPracticeService.getTodayStatus(username));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        try {
            String username = getUsername(request);
            dailyPracticeService.checkAndUpdateCompletion(username);
            return ResponseEntity.ok(dailyPracticeService.getTodayStatus(username));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/admin/assign")
    public ResponseEntity<?> assign(HttpServletRequest request,
                                    @RequestBody Map<String, String> body) {
        try {
            String username = getUsername(request);
            LocalDate date = body.containsKey("date")
                    ? LocalDate.parse(body.get("date")) : LocalDate.now();

            dailyPracticeService.assignProblem(
                    username, date,
                    body.get("title"), body.get("titleSlug"),
                    body.get("difficulty"), body.get("url")
            );
            return ResponseEntity.ok("Daily problem assigned successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}