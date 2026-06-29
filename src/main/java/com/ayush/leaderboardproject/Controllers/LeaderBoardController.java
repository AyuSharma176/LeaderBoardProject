package com.ayush.leaderboardproject.Controllers;

import com.ayush.leaderboardproject.Model.LeaderBoard;
import com.ayush.leaderboardproject.Service.LeaderBoardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderBoardController {
    private final LeaderBoardService leaderBoardService;

    public LeaderBoardController(LeaderBoardService leaderBoardService) {
        this.leaderBoardService = leaderBoardService;
    }

    @GetMapping
    public ResponseEntity<List<LeaderBoard>> getAllEntries(){
        return ResponseEntity.ok(leaderBoardService.getAllLeaderBoard());
    }

    @PostMapping("/add/{leetcodeUsername}")
    public ResponseEntity<?> addEntry(@PathVariable String leetcodeUsername){
        try {
            LeaderBoard newEntry = leaderBoardService.addEntry(leetcodeUsername);
            return ResponseEntity.ok(newEntry);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/refresh/{leetcodeUsername}")
    public ResponseEntity<?> refreshEntry(@PathVariable String leetcodeUsername){
        try {
            LeaderBoard updatedEntry = leaderBoardService.refreshEntry(leetcodeUsername);
            return ResponseEntity.ok(updatedEntry);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteEntry(@PathVariable Integer id){
        try {
            leaderBoardService.deleteEntry(id);
            return ResponseEntity.ok("Entry deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
