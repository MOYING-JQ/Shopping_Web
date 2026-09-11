package com.moying.project_test.util;

import com.moying.project_test.common.Result;
import com.moying.project_test.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 墨莹
 * @date 2026/8/25 19:10
 */

@Component
public class JwtUtil {

    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;


    @Autowired
    public JwtUtil(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        assert jwtConfig.getSecret() != null;
        this.secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    //获取accessToken
    public String getAccessToken(String username) {
        Map<String,Object> claims = new HashMap<>();
        claims.put("token_type","access_token");
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()*1000))
                .signWith(secretKey)
                .compact();
    }

    //获取refreshToken
    public String getRefreshToken(String username) {
        Map<String,Object> claims = new HashMap<>();
        long loginAt = System.currentTimeMillis();
        claims.put("token_type","refresh_token");
        claims.put("loginAt", loginAt);
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .expiration(new Date(System.currentTimeMillis() + 7*24*60*60*1000))
                .signWith(secretKey)
                .compact();
    }

    //解析Token
    public Claims parseToken(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //从请求头里获取accessToken
    public String getAccessTokenFromHeader(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        // 截取 "Bearer " 后面的实际 token
        String token = authHeader.substring(7);

        return token;
    }

    //刷新token
    public String refreshToken(Claims claims){
        Map<String,Object> claim = new HashMap<>();
        String username = claims.getSubject();
        // ✅注意key：loginAt 和登录生成处保持一致，驼峰，不是 login_at
        Long loginAt = claims.get("loginAt", Long.class);

        claim.put("token_type","refresh_token");
        claim.put("loginAt", loginAt);

        return Jwts.builder()
                .subject(username)
                .claims(claim)
                .expiration(new Date(System.currentTimeMillis() + 7L *24*60*60*1000))
                .signWith(secretKey)
                .compact();
    }


}
