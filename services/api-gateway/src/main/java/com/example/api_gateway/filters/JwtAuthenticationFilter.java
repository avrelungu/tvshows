package com.example.api_gateway.filters;

import com.example.api_gateway.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    private final JwtService jwtService;

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh"
    );

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (isExcludedPath(request.getPath().toString())) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtService.verifyToken(token);

            String username = claims.getIssuer();
            String firstName = claims.get("firstName", String.class);
            String lastName = claims.get("lastName", String.class);
            String role = claims.get("role", String.class);
            String membership = claims.get("membership", String.class);

            log.info("JWT Claims: username={}, firstName={}, lastName={}, role={}, membership={}",
                    username, firstName, lastName, role, membership);

            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-Auth-Username", username != null ? username : "")
                    .header("X-Auth-FirstName", firstName != null ? firstName : "")
                    .header("X-Auth-LastName", lastName != null ? lastName : "")
                    .header("X-Auth-Role", role != null ? role : "")
                    .header("X-Auth-Membership", membership != null ? membership : "")
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (JwtException e) {
            log.error(e.getMessage());
            return onError(exchange, "Invalid JWT token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error(e.getMessage());
            return onError(exchange, "Error while validating the JWT Token: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();

        response.setStatusCode(httpStatus);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json");

        String errorMessage = "{\"error\":\"" + error + "\",\"status\":" + httpStatus.value() + "}";
        DataBuffer buffer = response.bufferFactory().wrap(errorMessage.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
