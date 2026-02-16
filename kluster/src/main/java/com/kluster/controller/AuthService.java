package com.kluster.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.kluster.models.Personnel;
import com.kluster.models.PersonnelRole;
import org.mindrot.jbcrypt.BCrypt;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.security.SecureRandom;

/**
 * Simple AuthService providing registration, authentication and token validation.
 *
 * Notes:
 * - This implementation uses an in-memory user store (`users`). Replace with
 *   a persistent repository (database) for production use.
 * - JWT secret must be provided through the `JWT_SECRET` environment variable.
 */
public class AuthService {
    private final Algorithm jwtAlg;
    private final JWTVerifier verifier;
    private final Map<String, Personnel> users = new ConcurrentHashMap<>(); // In-memory user store (Replace with DB in production)
    private final Map<String, RefreshTokenRecord> refreshStore = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService() {
        String secret = System.getenv("JWT_SECRET");

        if (secret == null || secret.trim().isEmpty()) {
            System.err.println("\u001B[31mJWT_SECRET not set - falling back to development secret\\u001B[0m");
            System.exit(500);
        }
        
        jwtAlg = Algorithm.HMAC256(secret);
        verifier = JWT.require(jwtAlg).build();
    }

    /**
     * Registers a new user with the given details and plaintext password. The password is hashed before storage. 
     * @param id
     * @param name
     * @param rank
     * @param role
     * @param plainPassword
     */
    public void register(String id, String name, String rank, PersonnelRole role, String plainPassword) {
        Personnel p = new Personnel(id, name, rank, role);
        String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
        p.setPasswordHash(hash);
        users.put(id, p);
    }

    /**
     * Find a user by their ID. Returns null if not found.
     * @param id
     * @return
     */
    public Personnel findById(String id) {
        return users.get(id);
    }

    /**
     * Authenticate by id and password. Returns a signed JWT on success, or null on failure.
     */
    public String authenticate(String id, String plainPassword) {
        Personnel p = users.get(id);
        if (p == null || p.getPasswordHash() == null) return null;
        if (!BCrypt.checkpw(plainPassword, p.getPasswordHash())) return null;
        return createToken(p, 60);
    }

    /**
     * Authenticate and return both an access token (JWT) and a refresh token.
     * Access tokens are short-lived; refresh tokens are opaque and rotated on use.
     */
    public AuthResponse authenticateWithRefresh(String id, String plainPassword, long accessMinutes, long refreshDays) {
        Personnel p = users.get(id);
        if (p == null || p.getPasswordHash() == null) return null;
        if (!BCrypt.checkpw(plainPassword, p.getPasswordHash())) return null;

        String access = createToken(p, accessMinutes);
        String refresh = issueRefreshToken(id, refreshDays);
        Instant now = Instant.now();
        return new AuthResponse(access, refresh, Date.from(now.plus(accessMinutes, ChronoUnit.MINUTES)), Date.from(now.plus(refreshDays, ChronoUnit.DAYS)), p);
    }

    /**
     * Create a JWT token for the given user, valid for the specified number of minutes.
     * @param user
     * @param minutesValid
     * @return
     */
    public String createToken(Personnel user, long minutesValid) {
        Instant now = Instant.now();
        Date expires = Date.from(now.plus(minutesValid, ChronoUnit.MINUTES));
        return JWT.create()
                .withSubject(user.getId())
                .withClaim("role", user.getRole() != null ? user.getRole().name() : "")
                .withIssuedAt(Date.from(now))
                .withExpiresAt(expires)
                .sign(jwtAlg);
    }

    /**
     * Issue an opaque refresh token for the given user. The token format is "{userId}:{random}".
     * The server stores only a bcrypt hash of the random portion and the expiry.
     */
    private String issueRefreshToken(String userId, long daysValid) {
        byte[] rnd = new byte[48];
        secureRandom.nextBytes(rnd);
        String randomPart = Base64.getUrlEncoder().withoutPadding().encodeToString(rnd);
        String token = userId + ":" + randomPart;

        String hash = BCrypt.hashpw(randomPart, BCrypt.gensalt(12));
        Instant expires = Instant.now().plus(daysValid, ChronoUnit.DAYS);
        refreshStore.put(userId, new RefreshTokenRecord(userId, hash, Date.from(expires)));
        return token;
    }

    /**
     * Refresh an access token using a refresh token. On success, rotates the refresh token and
     * returns a new pair (access, refresh). Returns null on failure.
     */
    public AuthResponse refreshWithToken(String refreshToken, long newAccessMinutes, long refreshDays) {
        if (refreshToken == null) return null;
        int idx = refreshToken.indexOf(":");
        if (idx <= 0) return null;
        String userId = refreshToken.substring(0, idx);
        String randomPart = refreshToken.substring(idx + 1);

        RefreshTokenRecord rec = refreshStore.get(userId);
        if (rec == null) return null;
        if (rec.expiresAt.before(new Date())) {
            refreshStore.remove(userId);
            return null;
        }
        if (!BCrypt.checkpw(randomPart, rec.hash)) return null;

        // rotate: issue new refresh token
        String newRefresh = issueRefreshToken(userId, refreshDays);
        Personnel p = users.get(userId);
        if (p == null) return null;
        String newAccess = createToken(p, newAccessMinutes);
        Instant now = Instant.now();
        return new AuthResponse(newAccess, newRefresh, Date.from(now.plus(newAccessMinutes, ChronoUnit.MINUTES)), Date.from(now.plus(refreshDays, ChronoUnit.DAYS)), p);
    }

    /** Revoke refresh tokens for a user (logout). */
    public void revokeRefreshTokens(String userId) {
        refreshStore.remove(userId);
    }

    /**
     * Validate a token and return the corresponding Personal if valid and present.
     */
    public Personnel validateToken(String token) {
        try {
            DecodedJWT jwt = verifier.verify(token);
            String id = jwt.getSubject();
            return users.get(id);
        } catch (JWTVerificationException ex) {
            return null;
        }
    }

    private static class RefreshTokenRecord {
        final String userId;
        final String hash; // bcrypt hash of random part
        final Date expiresAt;

        RefreshTokenRecord(String userId, String hash, Date expiresAt) {
            this.userId = userId;
            this.hash = hash;
            this.expiresAt = expiresAt;
        }
    }

    public static class AuthResponse {
        public final String accessToken;
        public final String refreshToken;
        public final Date accessExpiresAt;
        public final Date refreshExpiresAt;
        public final Personnel user;

        public AuthResponse(String accessToken, String refreshToken, Date accessExpiresAt, Date refreshExpiresAt, Personnel user) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.accessExpiresAt = accessExpiresAt;
            this.refreshExpiresAt = refreshExpiresAt;
            this.user = user;
        }
    }
}
