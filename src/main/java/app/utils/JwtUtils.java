package app.utils;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtUtils {
     @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(String role, UUID id) {
        return Jwts.builder()
                .subject(id.toString())
                .claim("role", role)
                .claim("id", id)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + Long.valueOf(expirationTime)))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        if (token == null) {
            return null;
        }
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationTime() {
        return this.expirationTime;
    }

    public String getUsernameFromCookie(HttpServletRequest request) {
        String token = null;
        if (request.getCookies() != null) {
            // System.out.println("\n\n\n\n");
            for (Cookie cookie : request.getCookies()) {
                // System.out.println(cookie.getValue());

                if ("Credentials".equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        }

        Claims claims = parseToken(token);
        String username = claims.get("username", String.class);

        return username;

    }
    
}
