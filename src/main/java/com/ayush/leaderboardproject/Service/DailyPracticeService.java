package com.ayush.leaderboardproject.Service;

import com.ayush.leaderboardproject.Config.AppHttpEntity;
import com.ayush.leaderboardproject.Config.AppRestTemplate;
import com.ayush.leaderboardproject.Config.LeetCodeQuery;
import com.ayush.leaderboardproject.Model.DailyPracticeStatus;
import com.ayush.leaderboardproject.Model.DailyProblem;
import com.ayush.leaderboardproject.Model.User;
import com.ayush.leaderboardproject.Repository.DailyPracticeStatusRepository;
import com.ayush.leaderboardproject.Repository.DailyProblemRepository;
import com.ayush.leaderboardproject.Repository.UserRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
public class DailyPracticeService {

    private final DailyProblemRepository dailyProblemRepository;
    private final DailyPracticeStatusRepository statusRepository;
    private final UserRepository userRepository;
    private final AppRestTemplate restTemplate;
    private final AppHttpEntity httpEntity;

    public DailyPracticeService(DailyProblemRepository dailyProblemRepository,
                                DailyPracticeStatusRepository statusRepository,
                                UserRepository userRepository,
                                AppRestTemplate restTemplate,
                                AppHttpEntity httpEntity) {
        this.dailyProblemRepository = dailyProblemRepository;
        this.statusRepository = statusRepository;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
        this.httpEntity = httpEntity;
    }

    public DailyProblem assignProblem(String adminUsername, LocalDate date,
                                      String title, String titleSlug,
                                      String difficulty, String url) throws Exception {
        User admin = userRepository.findByUsername(adminUsername);
        if (admin == null || !admin.getRole().equals("ROLE_ADMIN")) {
            throw new Exception("Only admins can assign the daily problem");
        }

        DailyProblem problem = dailyProblemRepository.findByDate(date)
                .orElse(DailyProblem.builder().date(date).build());

        problem.setTitle(title);
        problem.setTitleSlug(titleSlug);
        problem.setDifficulty(difficulty);
        problem.setUrl(url);

        return dailyProblemRepository.save(problem);
    }

    public Map<String, Object> getTodayStatus(String username) {
        LocalDate today = LocalDate.now();
        User user = userRepository.findByUsername(username);

        DailyProblem problem = dailyProblemRepository.findByDate(today).orElse(null);

        boolean completed = false;
        if (problem != null) {
            completed = statusRepository.findByUserAndDate(user, today)
                    .map(DailyPracticeStatus::isCompleted)
                    .orElse(false);
        }

        return Map.of(
                "problem", problem == null ? Map.of() : Map.of(
                        "title", problem.getTitle(),
                        "difficulty", problem.getDifficulty(),
                        "url", problem.getUrl()
                ),
                "completed", completed,
                "currentStreak", user.getCurrentStreak(),
                "longestStreak", user.getLongestStreak()
        );
    }

    public void checkAndUpdateCompletion(String leetcodeUsername) {
        LocalDate today = LocalDate.now();
        DailyProblem problem = dailyProblemRepository.findByDate(today).orElse(null);
        if (problem == null) return;

        User user = userRepository.findByUsername(leetcodeUsername);
        if (user == null) return;

        // already marked complete today — skip API call
        boolean alreadyDone = statusRepository.findByUserAndDate(user, today)
                .map(DailyPracticeStatus::isCompleted)
                .orElse(false);
        if (alreadyDone) return;

        try {
            HttpEntity<String> request = httpEntity.create(
                    LeetCodeQuery.getRecentSubmissions(leetcodeUsername, 20));

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://leetcode.com/graphql", request, Map.class);

            Map data = (Map) response.getBody().get("data");
            List<Map> submissions = (List<Map>) data.get("recentAcSubmissionList");

            boolean solvedToday = submissions.stream().anyMatch(sub -> {
                String slug = (String) sub.get("titleSlug");
                long ts = Long.parseLong((String) sub.get("timestamp"));
                LocalDate solvedDate = Instant.ofEpochSecond(ts)
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                return slug.equals(problem.getTitleSlug()) && solvedDate.equals(today);
            });

            if (solvedToday) {
                markCompleted(user, today);
            }
        } catch (Exception e) {
            System.err.println("Daily practice check failed for " +
                    leetcodeUsername + ": " + e.getMessage());
        }
    }

    private void markCompleted(User user, LocalDate today) {
        DailyPracticeStatus status = statusRepository.findByUserAndDate(user, today)
                .orElse(DailyPracticeStatus.builder().user(user).date(today).build());
        status.setCompleted(true);
        status.setCompletedAt(LocalDateTime.now());
        statusRepository.save(status);

        LocalDate yesterday = today.minusDays(1);
        if (yesterday.equals(user.getLastCompletedDate())) {
            user.setCurrentStreak(user.getCurrentStreak() + 1);
        } else if (!today.equals(user.getLastCompletedDate())) {
            user.setCurrentStreak(1);
        }
        user.setLastCompletedDate(today);
        user.setLongestStreak(Math.max(user.getLongestStreak(), user.getCurrentStreak()));
        userRepository.save(user);
    }


    public void resetMissedStreaks() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            if (user.getCurrentStreak() > 0 &&
                    !yesterday.equals(user.getLastCompletedDate())) {
                user.setCurrentStreak(0);
                userRepository.save(user);
            }
        }
    }
}