package com.training.demo.security;

import com.training.demo.entity.User;
import com.training.demo.exception.TokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtProvider {

    @Value("${jwt.accessKey}")
    private String accessKeyBase64;

    @Value("${jwt.refreshKey}")
    private String refreshKeyBase64;

    @Value("${jwt.expiryMinutes}")
    private long expiryMinutes;

    @Value("${jwt.expiryDay}")
    private long expiryDay;

    private Key accessKey;
    private Key refreshKey;

    @PostConstruct
    public void init() {
        accessKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(accessKeyBase64));
        refreshKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(refreshKeyBase64));
    }

    public String generateAccessToken(User user) {
        log.info("Generating accessToken running");

        String roles = user.getUserHasRoles().stream()
                .map(role -> "ROLE_" + role.getRole().getName())
                .collect(Collectors.joining(","));
        return buildToken(user.getUsername(), roles, accessKey, getAccessTokenExpiryDate(), user.getId());
    }

    public String generateRefreshToken(String username) {
        log.info("Generating refreshToken running");

        return buildToken(username, null, refreshKey, getRefreshTokenExpiryDate(), null);
    }

    private String buildToken(String subject, String roles, Key key, Date expiryDate, Long userId) {
        JwtBuilder builder = Jwts.builder()
                .setId(java.util.UUID.randomUUID().toString())
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512);

        if (roles != null) {
            builder.claim("roles", roles);
        }

        if (userId != null) {
            builder.claim("userId", userId);
        }

        return builder.compact();
    }

    public Date getAccessTokenExpiryDate() {
        long expiryMillis = System.currentTimeMillis() + 1000 * 60 * expiryMinutes;
        return new Date(expiryMillis);
    }

    public Date getRefreshTokenExpiryDate() {
        long expiryMillis = System.currentTimeMillis() + 1000 * 60 * 60 * 24 * expiryDay;
        return new Date(expiryMillis);
    }

    public boolean validateToken(String token, boolean isAccessToken) {
        try {
            Key key = isAccessToken ? accessKey : refreshKey;
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new TokenException("Token has expired");
        } catch (MalformedJwtException e) {
            throw new TokenException("Invalid token format");
        } catch (SignatureException e) {
            throw new TokenException("Invalid token signature");
        } catch (UnsupportedJwtException e) {
            throw new TokenException("Unsupported token");
        } catch (IllegalArgumentException e) {
            throw new TokenException("Token is empty or null");
        }
    }

    public String getUsernameFromToken(String token, boolean isAccessToken) {
        Claims claims = extractAllClaims(token, isAccessToken);
        return claims.getSubject();
    }

    public String getRolesFromToken(String token) {
        Claims claims = extractAllClaims(token, true);
        return claims.get("roles", String.class);
    }

    public boolean isTokenExpired(String token, boolean isAccessToken) {
        try {
            return extractAllClaims(token, isAccessToken).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    private Claims extractAllClaims(String token, boolean isAccessToken) {
        Key key = isAccessToken ? accessKey : refreshKey;
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
