package com.example.tvshows_auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "jwt")
@Component
@Data
public class JwtProperties {
    private long accessTokenExpiration = 1800; // 30 minutes in seconds
    private long refreshTokenExpiration = 604800; // 7 days in seconds
    private Claims claims = new Claims();
    
    @Data
    public static class Claims {
        private String firstName = "firstName";
        private String lastName = "lastName";
        private String role = "role";
        private String membership = "membership";
        private String tokenId = "tokenId";
        private String type = "type";
        private String refreshType = "refresh";
    }
}