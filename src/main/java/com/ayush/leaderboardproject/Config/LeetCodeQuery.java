package com.ayush.leaderboardproject.Config;

public class LeetCodeQuery {

    public static String getUserStats(String username) {
        return """
            {
              "query": "{ matchedUser(username: \\"%s\\") { submitStats { acSubmissionNum { difficulty count } } profile { ranking } } }",
              "variables": {}
            }
            """.formatted(username);
    }
}