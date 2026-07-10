package com.ayush.leaderboardproject.Service;

import com.ayush.leaderboardproject.Model.User;
import com.ayush.leaderboardproject.Repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DailyPracticeScheduler {

    private final UserRepository userRepository;
    private final DailyPracticeService dailyPracticeService;

    public DailyPracticeScheduler(UserRepository userRepository,
                                  DailyPracticeService dailyPracticeService) {
        this.userRepository = userRepository;
        this.dailyPracticeService = dailyPracticeService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void checkAllCompletions() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            dailyPracticeService.checkAndUpdateCompletion(user.getUsername());
        }
    }

    @Scheduled(cron = "0 5 0 * * *")
    public void resetMissedStreaks() {
        dailyPracticeService.resetMissedStreaks();
    }
}