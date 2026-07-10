package com.ayush.leaderboardproject.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="daily_problem")
public class DailyProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private LocalDate date;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String titleSlug;

    @Column(nullable = false)
    private String difficulty;

    @Column(nullable = false)
    private String url;
}
