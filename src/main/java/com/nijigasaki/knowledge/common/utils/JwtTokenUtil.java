package com.nijigasaki.knowledge.common.utils;

import com.nijigasaki.knowledge.model.dto.UserDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil {
    private static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60; // token 有效期

    @Value("${jwt.secret}")
    private String secret;

    // 从 token 中解析出用户名
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // 从 token 中解析出过期时间
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    // 从 token 中解析出指定字段的值
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // 解析 token，返回包含 token 中所有信息的 Claims 对象
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token).getBody();
    }

    // 验证 token 是否过期
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    // 生成 token
    public String generateToken(UserDTO userDTO) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDTO.getUsername());
    }

    // 根据传入的信息生成 token
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + JWT_TOKEN_VALIDITY * 1000);

        JwtBuilder builder = Jwts.builder()
                .setSubject(subject)
                .setClaims(claims)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS512);
        return builder.compact();
    }

    // 验证 token 是否有效
    public Boolean validateToken(String token, UserDTO userDTO) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDTO.getUsername()) && !isTokenExpired(token));
    }
}
