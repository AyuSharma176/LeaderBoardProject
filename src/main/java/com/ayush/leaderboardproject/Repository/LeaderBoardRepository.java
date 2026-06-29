package com.ayush.leaderboardproject.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ayush.leaderboardproject.Model.LeaderBoard;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaderBoardRepository extends JpaRepository<LeaderBoard, Integer> {
    LeaderBoard findByLeetcodeUsername(String leetcodeUsername);
    boolean existsByLeetcodeUsername(String leetcodeUsername);
}
