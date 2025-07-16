package com.example.tvshows_auth.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.tvshows_auth.dto.UserDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class UserAuthProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(UserDto user) {
        Date now = Date.from(Instant.now());
        Date validity = new Date(now.getTime() + 1000 * 60 * 30); // 30 minutes

        log.info("user role {}", user.getRole());

        return JWT.create()
                .withIssuer(user.getUsername())
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .withClaim("firstName", user.getFirstName())
                .withClaim("lastName", user.getLastName())
                .withClaim("role", user.getRole())
                .withClaim("membership", user.getMembership())
                .sign(Algorithm.HMAC256(secretKey));

    }

    public String createRefreshToken(UserDto user) {
        Date now = Date.from(Instant.now());
        Date validity = new Date(now.getTime() + 1000L * 60 * 60 * 24 * 7);

        return JWT.create()
                .withIssuer(user.getUsername())
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .withClaim("tokenId", UUID.randomUUID().toString())
                .withClaim("type", "refresh")
                .sign(Algorithm.HMAC256(secretKey));
    }

    public boolean validateRefreshToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            
            return "refresh".equals(jwt.getClaim("type").asString());
        } catch (Exception e) {
            log.error("Invalid refresh token: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromRefreshToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            
            return jwt.getIssuer();
        } catch (Exception e) {
            log.error("Cannot extract username from refresh token: {}", e.getMessage());
            return null;
        }
    }

    public UsernamePasswordAuthenticationToken getToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        JWTVerifier verifier = JWT.require(algorithm).build();

        DecodedJWT jwt = verifier.verify(token);

        UserDto user = new UserDto();
        user.setUsername(jwt.getIssuer());
        user.setFirstName(jwt.getClaim("firstName").asString());
        user.setLastName(jwt.getClaim("lastName").asString());

        return new UsernamePasswordAuthenticationToken(user, "", Collections.emptyList());
    }
}
