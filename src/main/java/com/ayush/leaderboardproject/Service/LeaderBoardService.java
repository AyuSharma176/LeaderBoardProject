package com.ayush.leaderboardproject.Service;

import com.ayush.leaderboardproject.Config.AppHttpEntity;
import com.ayush.leaderboardproject.Config.AppHttpHeaders;
import com.ayush.leaderboardproject.Config.AppRestTemplate;
import com.ayush.leaderboardproject.Config.LeetCodeQuery;
import com.ayush.leaderboardproject.Model.LeaderBoard;
import com.ayush.leaderboardproject.Repository.LeaderBoardRepository;
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
                .rating(((Number) stats.get("rating")).doubleValue())
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
        entry.setSolved(((Number) stats.get("solved")).intValue());
        entry.setRating(((Number) stats.get("rating")).doubleValue());
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

    private Map<String, Object> fetchLeetCodeStats(String leetcodeUsername) {
        HttpEntity<String> request =httpEntity.create(LeetCodeQuery.getUserStats(leetcodeUsername));

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://leetcode.com/graphql",
                request,
                Map.class
        );

        Map data = (Map) response.getBody().get("data");
        Map matchedUser = (Map) data.get("matchedUser");
        Map profile = (Map) matchedUser.get("profile");
        Map submitStats = (Map) matchedUser.get("submitStats");
        List<Map> acSubmissions = (List<Map>) submitStats.get("acSubmissionNum");

        int totalSolved  = ((Number) ((Map) acSubmissions.get(0)).get("count")).intValue();
        int easySolved   = ((Number) ((Map) acSubmissions.get(1)).get("count")).intValue();
        int mediumSolved = ((Number) ((Map) acSubmissions.get(2)).get("count")).intValue();
        int hardSolved   = ((Number) ((Map) acSubmissions.get(3)).get("count")).intValue();

        int ranking = ((Number) profile.get("ranking")).intValue();

        double rating = 1500/(1+Math.log(ranking+1));

        int score= calculateScore(totalSolved,easySolved,mediumSolved,hardSolved,(int) rating);

        return Map.of(
                "solved",totalSolved,
                "rating",rating,
                "score", score
        );

    }

    private int calculateScore(int totalSolved, int easySolved,int mediumSolved,int hardSolved,int rating){
        int difficultyScore = easySolved*10 + mediumSolved*25 + hardSolved*50;
        int ratingScore = rating*15;

        return  (difficultyScore+ratingScore)/1000;
    }

}
