package com.ayush.leaderboardproject.Repository;

import com.ayush.leaderboardproject.Model.DailyActivity;
import com.ayush.leaderboardproject.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyActivityRepository extends JpaRepository<DailyActivity, Integer> {
    List<DailyActivity> findByUserOrderByDateDesc(User user);
    Optional<DailyActivity> findByUserAndDate(User user, LocalDate date);
}