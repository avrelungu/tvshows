package com.example.api_gateway.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {
    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        String encodedSecret = Base64.getEncoder().encodeToString(jwtSecret.getBytes());
        signingKey = Keys.hmacShaKeyFor(encodedSecret.getBytes());
    }

    public Claims verifyToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (claims.getExpiration().before(new Date())) {
                throw new ExpiredJwtException(null, claims, "Token has expired");
            }

            return claims;
        } catch (ExpiredJwtException e) {
            throw new JwtException("JWT token is expired: " + e.getMessage());
        } catch (SignatureException e) {
            throw new JwtException("JWT token signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            throw new JwtException("JWT token malformed: " + e.getMessage());
        } catch (Exception e) {
            throw new JwtException("JWT validation failed: " + e.getMessage());
        }
    }

    public String extractUsername(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getIssuer();
        } catch (Exception e) {
            return null;
        }
    }
}
