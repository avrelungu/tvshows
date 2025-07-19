package com.example.api_gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "jwt.auth")
@Component
@Data
public class JwtAuthProperties {
    private List<String> excludedPaths;

}