package com.ayush.leaderboardproject.Repository;

import com.ayush.leaderboardproject.Model.DailyProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyProblemRepository extends JpaRepository<DailyProblem, Integer> {
    Optional<DailyProblem> findByDate(LocalDate date);
}