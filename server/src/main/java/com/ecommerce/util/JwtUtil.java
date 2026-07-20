package com.ecommerce.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 * 用于生成和验证登录 Token
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;                          // 从配置文件读取密钥

    @Value("${jwt.expiration}")
    private long expiration;                        // 从配置文件读取过期时间

    /** 获取签名密钥 */
    private SecretKey getKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 Token
     * @param userId   用户ID
     * @param username 用户名
     * @param role     用户角色
     * @return JWT Token 字符串
     */
    public String generateToken(Integer userId, String username, String role) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)                        // 主题：用户名
                .claim("userId", userId)                  // 自定义字段：用户ID
                .claim("role", role)                      // 自定义字段：角色
                .issuedAt(now)                            // 签发时间
                .expiration(expireDate)                   // 过期时间
                .signWith(getKey())                       // 签名
                .compact();
    }

    /**
     * 从 Token 中解析 Claims
     * @param token JWT Token
     * @return Claims 对象，解析失败返回 null
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证 Token 是否有效
     * @param token JWT Token
     * @return true=有效, false=无效或已过期
     */
    public boolean validateToken(String token) {
        return parseToken(token) != null;
    }

    /** 从 Token 中获取用户ID */
    public Integer getUserId(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("userId", Integer.class) : null;
    }

    /** 从 Token 中获取用户名 */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /** 从 Token 中获取用户角色 */
    public String getRole(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("role", String.class) : null;
    }
}
