package com.retail.user_service.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import com.retail.user_service.global.exception.BaseException;
import com.retail.user_service.global.exception.ErrorCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtProvider {
    private final Key key;
    private final long expirationTime;
    private final TokenBlacklist tokenBlacklist;

    public JwtProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration_time}") long expirationTime,
            TokenBlacklist tokenBlacklist
    ) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = expirationTime;
        this.tokenBlacklist = tokenBlacklist;
    }

    // 1. 토큰 생성 (로그인 성공 시 호출)
    public String createToken(String userId, String email, String role) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(userId)
                .claim("email", email)
                .claim("role", role)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 2. 토큰 유효성 검사 (Gateway 또는 내부 서비스 필터에서 사용)
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            log.debug("JWT validation failed");
            return false;
        }

        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            String jti = claims.getId();
            if (jti != null && tokenBlacklist.isBlacklisted(jti)) {
                return false;
            }
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug(
                    "JWT validation failed: type={}, message={}",
                    e.getClass().getSimpleName(),
                    e.getMessage()
            );
            return false;
        }
    }

    /** 로그아웃 시 jti를 블랙리스트에 넣어 만료 시각까지 거절한다. */
    public void invalidateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            String jti = claims.getId();
            if (jti == null || jti.isBlank()) {
                throw new BaseException(ErrorCode.INVALID_REQUEST, "토큰을 다시 발급받은 뒤 로그아웃해 주세요.");
            }
            Date exp = claims.getExpiration();
            if (exp == null) {
                throw new BaseException(ErrorCode.INVALID_REQUEST, "만료 정보가 없는 토큰입니다.");
            }
            tokenBlacklist.blacklistUntil(jti, exp.toInstant());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug(
                    "invalidateToken skipped: type={}, message={}",
                    e.getClass().getSimpleName(),
                    e.getMessage()
            );
        }
    }

    // 3. 토큰에서 Authentication 객체 추출 (SecurityContext 보관용)
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                            .parseClaimsJws(token).getBody();
        
        // 권한 정보 추출
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("role").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }
}
