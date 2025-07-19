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
    private final JwtProperties jwtProperties;

    @Value("${jwt.secret}")
    private String secretKey;

    public UserAuthProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(UserDto user) {
        Date now = Date.from(Instant.now());
        Date validity = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration() * 1000);

        JwtProperties.Claims claims = jwtProperties.getClaims();
        return JWT.create()
                .withIssuer(user.getUsername())
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .withClaim(claims.getFirstName(), user.getFirstName())
                .withClaim(claims.getLastName(), user.getLastName())
                .withClaim(claims.getRole(), user.getRole())
                .withClaim(claims.getMembership(), user.getMembership())
                .sign(Algorithm.HMAC256(secretKey));
    }

    public String createRefreshToken(UserDto user) {
        Date now = Date.from(Instant.now());
        Date validity = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration() * 1000);

        JwtProperties.Claims claims = jwtProperties.getClaims();
        return JWT.create()
                .withIssuer(user.getUsername())
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .withClaim(claims.getTokenId(), UUID.randomUUID().toString())
                .withClaim(claims.getType(), claims.getRefreshType())
                .sign(Algorithm.HMAC256(secretKey));
    }

    public boolean validateRefreshToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            
            return jwtProperties.getClaims().getRefreshType().equals(jwt.getClaim(jwtProperties.getClaims().getType()).asString());
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
        JwtProperties.Claims claims = jwtProperties.getClaims();
        user.setFirstName(jwt.getClaim(claims.getFirstName()).asString());
        user.setLastName(jwt.getClaim(claims.getLastName()).asString());

        return new UsernamePasswordAuthenticationToken(user, "", Collections.emptyList());
    }
}
