package com.ayush.leaderboardproject.Config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class AppHttpHeaders extends HttpHeaders {
    public AppHttpHeaders(){
        this.setContentType(MediaType.APPLICATION_JSON);
        this.set("Referer", "https://leetcode.com");
    }
}
