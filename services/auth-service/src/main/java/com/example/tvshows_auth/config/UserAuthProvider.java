package com.example.tvshows_auth.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.tvshows_auth.dto.UserDto;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;

@Component
public class UserAuthProvider {

    @Value("${security.jwt.token.secret-key:secret-key}")
    private String secretKey;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());

    }

    public String createToken(UserDto user) {
        Date now = Date.from(Instant.now());
        Date validity = new Date(now.getTime() + 1000 * 60 * 60);

        return JWT.create()
                .withIssuer(user.getUsername())
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .withClaim("firstName", user.getFirstName())
                .withClaim("lastName", user.getLastName())
                .sign(Algorithm.HMAC256(secretKey));

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
