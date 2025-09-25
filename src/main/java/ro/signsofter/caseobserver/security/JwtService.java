package ro.signsofter.caseobserver.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTtlMs;
    private final long refreshTtlMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-ttl-ms:900000}") long accessTtlMs,
            @Value("${jwt.refresh-ttl-ms:2592000000}") long refreshTtlMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTtlMs = accessTtlMs;
        this.refreshTtlMs = refreshTtlMs;
    }

    public String generateAccessToken(String username, String role) {
        return generateToken(username, role, accessTtlMs, Map.of("typ", "access"));
    }

    public String generateRefreshToken(String username, String role) {
        return generateToken(username, role, refreshTtlMs, Map.of("typ", "refresh"));
    }

    private String generateToken(String username, String role, long ttl, Map<String, Object> extra) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claims(extra)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(ttl)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}


