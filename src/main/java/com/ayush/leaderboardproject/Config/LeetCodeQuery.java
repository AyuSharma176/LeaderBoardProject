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
    public static String getRecentSubmissions(String username, int limit) {
        return """
        {
          "query": "query recentAcSubmissions($username: String!, $limit: Int!) { recentAcSubmissionList(username: $username, limit: $limit) { titleSlug timestamp } }",
          "variables": { "username": "%s", "limit": %d }
        }
        """.formatted(username, limit);
    }
}