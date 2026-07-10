package com.ayush.leaderboardproject.Service;

import com.ayush.leaderboardproject.Model.LeaderBoard;
import com.ayush.leaderboardproject.Repository.LeaderBoardRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Component
public class LeaderBoardScheduler {
    private final LeaderBoardRepository leaderBoardRepository;
    private final LeaderBoardService leaderBoardService;
    private final ProfileService profileService;
    public LeaderBoardScheduler(LeaderBoardRepository leaderBoardRepository, LeaderBoardService leaderBoardService, ProfileService profileService) {
        this.leaderBoardRepository = leaderBoardRepository;
        this.leaderBoardService = leaderBoardService;
        this.profileService = profileService;
    }

    @Scheduled(cron = "${leaderboard.refresh.cron}")
    public void refreshAllStats() {
        System.out.println("Starting scheduled leaderboard refresh...");

        List<LeaderBoard> allEntries = leaderBoardRepository.findAll();

        for (LeaderBoard entry : allEntries) {
            try {
                leaderBoardService.refreshEntry(entry.getLeetcodeUsername());

                // Record today's daily unique as activity
                if (entry.getUser() != null) {
                    profileService.recordDailyActivity(
                            entry.getUser(),
                            entry.getDailyUnique()
                    );
                }
                System.out.println("Refreshed: " + entry.getLeetcodeUsername());
            } catch (Exception e) {
                System.err.println("Failed to refresh " +
                        entry.getLeetcodeUsername() + ": " + e.getMessage());
            }
        }

        System.out.println("Scheduled leaderboard refresh complete.");
    }

    @PostMapping("/refresh-all")
    public ResponseEntity<?> refreshAll() {
        try {
            List<LeaderBoard> all = leaderBoardService.getAllLeaderBoard();
            for (LeaderBoard entry : all) {
                leaderBoardService.refreshEntry(entry.getLeetcodeUsername());
            }
            return ResponseEntity.ok("All entries refreshed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
