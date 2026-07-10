package com.ayush.leaderboardproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LeaderboardprojectApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeaderboardprojectApplication.class, args);
    }

}
