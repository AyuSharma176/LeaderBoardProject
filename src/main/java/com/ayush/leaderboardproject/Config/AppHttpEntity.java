package com.ayush.leaderboardproject.Config;

import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;

@Component
public class AppHttpEntity {
    private final AppHttpHeaders headers;
    public AppHttpEntity(AppHttpHeaders headers){
        this.headers=headers;
    }
    public HttpEntity<String> create(String body){
        return new HttpEntity<>(body,headers);
    }
}
