package com.example.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具：签发 token、校验 token
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expireMillis;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expire-hours:24}") long expireHours) {
        // HS256 要求密钥至少 32 字节
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMillis = expireHours * 3600 * 1000;
    }

    /** 签发 token，payload 里放 uid 和 username */
    public String generateToken(Long id, String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("uid", id)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMillis))
                .signWith(key)
                .compact();
    }

    /** 校验并解析 token：成功返回 Claims，失败（过期/伪造）返回 null */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    /** 从 Claims 里安全取出 uid（JSON 反序列化可能变成 Integer，统一转成 Long） */
    public Long getUid(Claims claims) {
        Object uid = claims.get("uid");
        if (uid == null) return null;
        if (uid instanceof Number) return ((Number) uid).longValue();
        return Long.parseLong(String.valueOf(uid));
    }
}
