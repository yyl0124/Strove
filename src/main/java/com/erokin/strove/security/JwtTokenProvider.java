package com.erokin.strove.security;

import com.erokin.strove.config.properties.AppSecurityProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token 提供者
 */
@Component
public class JwtTokenProvider {

    private final AppSecurityProperties securityProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(AppSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
        this.secretKey = Keys.hmacShaKeyFor(
            securityProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * 生成JWT Token
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + securityProperties.getJwt().getExpirationMs());

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("username", username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
