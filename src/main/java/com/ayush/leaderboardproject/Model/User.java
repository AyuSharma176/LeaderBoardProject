package com.ayush.leaderboardproject.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false,unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column
    private String profilePicture;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int currentStreak;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int longestStreak;

    @Column
    private java.time.LocalDate lastCompletedDate;
}
