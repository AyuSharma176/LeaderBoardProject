package com.ayush.leaderboardproject.Service;

import com.ayush.leaderboardproject.Config.AppHttpEntity;
import com.ayush.leaderboardproject.Config.AppHttpHeaders;
import com.ayush.leaderboardproject.Config.AppRestTemplate;
import com.ayush.leaderboardproject.Config.LeetCodeQuery;
import com.ayush.leaderboardproject.Model.LeaderBoard;
import com.ayush.leaderboardproject.Model.User;
import com.ayush.leaderboardproject.Repository.LeaderBoardRepository;
import io.micrometer.observation.annotation.ObservationKeyValue;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.http.HttpHeaders;
import java.util.Comparator;
import java.util.List;
import java.util.Map;



@Service
public class LeaderBoardService {

    private final LeaderBoardRepository leaderBoardRepo;
    private final AppRestTemplate restTemplate;
    private final AppHttpHeaders headers;
    private final AppHttpEntity httpEntity;

    public LeaderBoardService(LeaderBoardRepository leaderBoardRepo, AppRestTemplate restTemplate, AppHttpHeaders headers, AppHttpEntity httpEntity) {
        this.leaderBoardRepo = leaderBoardRepo;
        this.restTemplate = restTemplate;
        this.headers = headers;
        this.httpEntity = httpEntity;
    }

    public List<LeaderBoard> getAllLeaderBoard() {
        return leaderBoardRepo.findAll()
                .stream()
                .sorted(Comparator.comparingInt(LeaderBoard::getScore).reversed())
                .toList();
    }

    public LeaderBoard addEntry(String leetcodeUsername) throws Exception {
        if(leaderBoardRepo.existsByLeetcodeUsername(leetcodeUsername)){
            throw new Exception("User with leetcode username " + leetcodeUsername + " already exists in the leaderboard.");
        }

        Map<String,Object> stats=fetchLeetCodeStats(leetcodeUsername);

        LeaderBoard newEntry = LeaderBoard.builder()
                .leetcodeUsername(leetcodeUsername)
                .solved(((Number) stats.get("solved")).intValue())
                .rank(0)
                .score(((Number) stats.get("score")).intValue())
                .build();
        LeaderBoard saved = leaderBoardRepo.save(newEntry);
        recalculateRanks();
        return saved;
    }
    public LeaderBoard refreshEntry(String leetcodeUsername) throws Exception {
        LeaderBoard entry = leaderBoardRepo.findByLeetcodeUsername(leetcodeUsername);
        if(entry==null) throw new Exception("User with leetcode username " + leetcodeUsername + " does not exist in the leaderboard.");


        Map<String, Object> stats = fetchLeetCodeStats(leetcodeUsername);

        int newEasy = ((Number) stats.get("easySolved")).intValue();
        int newMedium = ((Number) stats.get("mediumSolved")).intValue();
        int newHard = ((Number) stats.get("hardSolved")).intValue();

        int dailyEasy = Math.max(0,newEasy-entry.getPrevEasySolved());
        int dailyMedium = Math.max(0,newMedium-entry.getPrevMediumSolved());
        int dailyHard = Math.max(0,newHard-entry.getPrevHardSolved());
        int dailyUnique = dailyEasy+dailyMedium+dailyHard;
        entry.setPrevEasySolved(newEasy);
        entry.setPrevMediumSolved(newMedium);
        entry.setPrevHardSolved(newHard);

        entry.setSolved(((Number) stats.get("solved")).intValue());
        entry.setEasySolved(newEasy);
        entry.setMediumSolved(newMedium);
        entry.setHardSolved(newHard);
        entry.setDailyUnique(dailyUnique);
        entry.setScore(((Number) stats.get("score")).intValue());
        LeaderBoard saved = leaderBoardRepo.save(entry);
        recalculateRanks();
        return saved;
    }

    public void deleteEntry(int id){
        leaderBoardRepo.deleteById(id);
        recalculateRanks();
    }


    private void recalculateRanks() {
        List<LeaderBoard> sorted = leaderBoardRepo.findAll()
                .stream()
                .sorted(Comparator.comparingInt(LeaderBoard::getScore).reversed())
                .toList();

        for(int i=0;i<sorted.size();i++){
            sorted.get(i).setRank(i+1);
            leaderBoardRepo.save(sorted.get(i));
        }
    }

    private Map<String, Object> fetchLeetCodeStats(String leetcodeUsername) throws Exception {
        HttpEntity<String> request =httpEntity.create(LeetCodeQuery.getUserStats(leetcodeUsername));

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://leetcode.com/graphql",
                request,
                Map.class
        );

        Map data = (Map) response.getBody().get("data");
        Map matchedUser = (Map) data.get("matchedUser");

        if(matchedUser==null){
            throw new Exception("User with leetcode username " + leetcodeUsername + " does not exist.");
        }
        Map profile = (Map) matchedUser.get("profile");
        Map submitStats = (Map) matchedUser.get("submitStats");
        List<Map> acSubmissions = (List<Map>) submitStats.get("acSubmissionNum");

        int totalSolved  = ((Number) ((Map) acSubmissions.get(0)).get("count")).intValue();
        int easySolved   = ((Number) ((Map) acSubmissions.get(1)).get("count")).intValue();
        int mediumSolved = ((Number) ((Map) acSubmissions.get(2)).get("count")).intValue();
        int hardSolved   = ((Number) ((Map) acSubmissions.get(3)).get("count")).intValue();

        int ranking = ((Number) profile.get("ranking")).intValue();


        int score= calculateScore(totalSolved,easySolved,mediumSolved,hardSolved);

        return Map.of(
                "solved",totalSolved,
                "score", score,
                "easySolved" , easySolved,
                "mediumSolved" , mediumSolved,
                "hardSolved" , hardSolved,
                "dailyUnique", 0
        );

    }

    private int calculateScore(int totalSolved, int easySolved,int mediumSolved,int hardSolved){
        int difficultyScore = easySolved*15 + mediumSolved*25 + hardSolved*60;

        return  (difficultyScore)/1000;
    }

    public LeaderBoard addEntryForUser(User saved, String leetcodeUsername) throws Exception {
        if(leaderBoardRepo.existsByLeetcodeUsername(leetcodeUsername)){
            throw new Exception("This LeetCode Username Already Exists in the Leaderboard");
        }

        Map<String , Object> stats = fetchLeetCodeStats(leetcodeUsername);
        int easy = ((Number) stats.get("easySolved")).intValue();
        int medium=((Number) stats.get("mediumSolved")).intValue();
        int hard = ((Number) stats.get("hardSolved")).intValue();
        LeaderBoard newEntry = LeaderBoard.builder()
                .user(saved)
                .leetcodeUsername(leetcodeUsername)
                .solved(((Number) stats.get("solved")).intValue())
                .easySolved(easy)
                .mediumSolved(medium)
                .hardSolved(hard)
                .prevEasySolved(easy)
                .prevMediumSolved(medium)
                .prevHardSolved(hard)
                .dailyUnique(0)
                .rank(0)
                .score(((Number) stats.get("score")).intValue())
                .build();
        LeaderBoard savedEntry = leaderBoardRepo.save(newEntry);
        recalculateRanks();
        return savedEntry;
    }
}
