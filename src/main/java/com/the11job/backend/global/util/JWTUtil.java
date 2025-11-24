package com.the11job.backend.global.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JWTUtil {

    private final SecretKey secretKey;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // getClaims는 서명 검증을 통과한 토큰만 처리하고, 오류 발생 시 명확하게 던지도록
    private Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (io.jsonwebtoken.SignatureException e) {
            log.error("JWT signature validation failed: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JWT Signature");
        } catch (ExpiredJwtException e) {
            // getEmail/getRole 호출 시 만료된 경우에도 예외를 던지도록 허용
            throw e;
        } catch (Exception e) {
            log.error("JWT parsing failed: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JWT token");
        }
    }

    public String getEmail(String token) {
        return getClaims(token).get("email", String.class); // 키 이름을 email로 명확히
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public Boolean isExpired(String token) {
        try {
            // 만료 체크 시 Jwts.parserBuilder()가 만료 예외를 던지도록 설정하지 않거나
            // 별도의 파서를 사용하여 예외가 발생하면 true를 반환하는 방식으로 변경합니다.

            // 현재 로직을 유지하면서 만료 예외를 포착하는 방법 (권장):
            // Jwts.parserBuilder()가 만료된 토큰에 대해 ExpiredJwtException을 던지도록 허용
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();

            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true; // 만료됨
        } catch (Exception e) {
            // 서명 오류나 다른 파싱 오류는 false를 반환하거나 다시 던져 필터가 처리하도록 합니다.
            log.error("JWT isExpired check failed with other exception: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    public String createJwt(String category, String email, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }
}