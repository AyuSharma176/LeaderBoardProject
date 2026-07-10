package com.ayush.leaderboardproject.Repository;

import com.ayush.leaderboardproject.Model.DailyPracticeStatus;
import com.ayush.leaderboardproject.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyPracticeStatusRepository extends JpaRepository<DailyPracticeStatus, Integer> {
    Optional<DailyPracticeStatus> findByUserAndDate(User user, LocalDate date);
}