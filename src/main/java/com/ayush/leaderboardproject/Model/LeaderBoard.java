package com.ayush.leaderboardproject.Model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="leaderboard")
public class LeaderBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = true)
    private User user;

    @Column(nullable = false,unique = true)
    private String leetcodeUsername;

    @Column(name = "user_rank", nullable = false)
    private int rank;

    @Column(nullable = false)
    private int solved;

    @Column(nullable = false)
    private double rating;

    @Column(nullable = false)
    private int score;
}
