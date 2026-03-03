// package com.kluster.controller;

// import com.auth0.jwt.JWT;
// import com.auth0.jwt.algorithms.Algorithm;
// import com.auth0.jwt.exceptions.JWTVerificationException;
// import com.auth0.jwt.interfaces.DecodedJWT;
// import com.auth0.jwt.interfaces.JWTVerifier;
// import com.kluster.Kluster;
// import com.kluster.models.Personnel;
// import com.kluster.models.PersonnelRole;
// import org.mindrot.jbcrypt.BCrypt;

// import java.time.Instant;
// import java.time.temporal.ChronoUnit;
// import java.util.Base64;
// import java.util.Date;
// import java.util.Map;
// import java.util.concurrent.ConcurrentHashMap;

// import java.security.SecureRandom;
// import java.sql.SQLException;

// /**
//  * Simple AuthService providing registration, authentication and token
//  * validation.
//  *
//  * Notes:
//  * - This implementation uses an in-memory user store (`users`). Replace with
//  * a persistent repository (database) for production use.
//  * - JWT secret must be provided through the `JWT_SECRET` environment variable.
//  */
// public class AuthService {
//     private final Algorithm jwtAlg;
//     private final JWTVerifier verifier;
//     private final Map<Integer, RefreshTokenRecord> refreshStore = new ConcurrentHashMap<>();
//     private final SecureRandom secureRandom = new SecureRandom();

//     private final Database database;
//     private final Kluster kluster;

//     public AuthService(Database database, Kluster kluster) {
//         this.database = database;
//         this.kluster = kluster;

//         // Prefer environment variable, fall back to system property for testability.
//         String secret = kluster.env().get("JWT_SECRET");
//         if (secret == null || secret.trim().isEmpty()) {
//             System.err.println("\u001B[31mJWT_SECRET not set - falling back to development secret\u001B[0m");
//             throw new IllegalStateException(
//                     "JWT_SECRET environment variable or system property is required for AuthService to function. Set JWT_SECRET to a strong random value in production.");
//         }

//         jwtAlg = Algorithm.HMAC256(secret);
//         verifier = JWT.require(jwtAlg).build();
//     }

//     /**
//      * Registers a new user with the given details and plaintext password. The
//      * password is hashed before storage.
//      * 
//      * @param id
//      * @param name
//      * @param rank
//      * @param role
//      * @param plainPassword
//      */
//     public void register(int id, String name, int rank, PersonnelRole role, String plainPassword,
//             int signedByPersonnelId) {
//         if (id <= 0 || name == null || name.trim().isEmpty() || rank < 0 || role == null
//                 || plainPassword == null || plainPassword.isEmpty()) {
//             throw new IllegalArgumentException("Input cannot be null, empty, or invalid");
//         }

//         if (!Security.isSafeForSQL(name) || role != null && !Security.isSafeForSQL(role.name())) {
//             throw new IllegalArgumentException("Input contains unsafe characters");
//         }

//         String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));

//         try {
//             database.addPersonnel(name, rank, role.getCode(), true, hash, signedByPersonnelId);
//         } catch (SQLException e) {
//             e.printStackTrace();
//             throw new RuntimeException("Failed to register user: " + e.getMessage());
//         }

//     }

//     /**
//      * Authenticate by id and password. Returns a signed JWT on success, or null on
//      * failure.
//      */
//     public String authenticate(int id, String plainPassword) {
//         if (plainPassword == null || plainPassword.isEmpty()) {
//             throw new IllegalArgumentException("Input cannot be null, empty or invalid");
//         }

//         if (plainPassword.length() > 256) {
//             throw new IllegalArgumentException("Input is too long");
//         }

//         if (!Security.isSafeForSQL(plainPassword)) {
//             throw new IllegalArgumentException("Input contains unsafe characters");
//         }

//         Personnel p = database.getPersonnelById(id);

//         if (p == null || p.getPasswordHash() == null) {
//             return null;
//         }

//         if (!BCrypt.checkpw(plainPassword, p.getPasswordHash())) {
//             return null;
//         }

//         return createToken(p, 60);
//     }

//     /**
//      * Authenticate and return both an access token (JWT) and a refresh token.
//      * Access tokens are short-lived; refresh tokens are opaque and rotated on use.
//      */
//     public AuthResponse authenticateWithRefresh(int id, String plainPassword, long accessMinutes, long refreshDays) {
//         Personnel p = database.getPersonnelById(id);
//         if (p == null || p.getPasswordHash() == null) {
//             return null;
//         }

//         if (!BCrypt.checkpw(plainPassword, p.getPasswordHash())) {
//             return null;
//         }

//         String access = createToken(p, accessMinutes);
//         String refresh = issueRefreshToken(id, refreshDays);
//         Instant now = Instant.now();

//         return new AuthResponse(access,
//                 refresh,
//                 Date.from(now.plus(accessMinutes, ChronoUnit.MINUTES)),
//                 Date.from(now.plus(refreshDays, ChronoUnit.DAYS)),
//                 p);
//     }

//     /**
//      * Create a JWT token for the given user, valid for the specified number of
//      * minutes.
//      * 
//      * @param user
//      * @param minutesValid
//      * @return
//      */
//     public String createToken(Personnel user, long minutesValid) {
//         Instant now = Instant.now();
//         Date expires = Date.from(now.plus(minutesValid, ChronoUnit.MINUTES));
//         return JWT.create()
//                 .withSubject(String.valueOf(user.getId()))
//                 .withClaim("role", user.getRole() != null ? user.getRole().name() : "")
//                 .withIssuedAt(Date.from(now))
//                 .withExpiresAt(expires)
//                 .sign(jwtAlg);
//     }

//     /**
//      * Issue an opaque refresh token for the given user. The token format is
//      * "{userId}:{random}".
//      * The server stores only a bcrypt hash of the random portion and the expiry.
//      */
//     private String issueRefreshToken(int userId, long daysValid) {
//         byte[] rnd = new byte[48];
//         secureRandom.nextBytes(rnd);
//         String randomPart = Base64.getUrlEncoder().withoutPadding().encodeToString(rnd);
//         String token = userId + ":" + randomPart;

//         String hash = BCrypt.hashpw(randomPart, BCrypt.gensalt(12));
//         Instant expires = Instant.now().plus(daysValid, ChronoUnit.DAYS);
//         refreshStore.put(userId, new RefreshTokenRecord(userId, hash, Date.from(expires)));
//         return token;
//     }

//     /**
//      * Refresh an access token using a refresh token. On success, rotates the
//      * refresh token and
//      * returns a new pair (access, refresh). Returns null on failure.
//      */
//     public AuthResponse refreshWithToken(String refreshToken, long newAccessMinutes, long refreshDays) {
//         if (refreshToken == null)
//             return null;
//         int idx = refreshToken.indexOf(":");
//         if (idx <= 0)
//             return null;
//         int userId = Integer.parseInt(refreshToken.substring(0, idx));
//         String randomPart = refreshToken.substring(idx + 1);

//         RefreshTokenRecord rec = refreshStore.get(userId);
//         if (rec == null)
//             return null;
//         if (rec.expiresAt.before(new Date())) {
//             refreshStore.remove(userId);
//             return null;
//         }
//         if (!BCrypt.checkpw(randomPart, rec.hash))
//             return null;

//         // rotate: issue new refresh token
//         String newRefresh = issueRefreshToken(userId, refreshDays);
//         Personnel p = database.getPersonnelById(userId);
//         if (p == null)
//             return null;
//         String newAccess = createToken(p, newAccessMinutes);
//         Instant now = Instant.now();
//         return new AuthResponse(newAccess, newRefresh, Date.from(now.plus(newAccessMinutes, ChronoUnit.MINUTES)),
//                 Date.from(now.plus(refreshDays, ChronoUnit.DAYS)), p);
//     }

//     /** Revoke refresh tokens for a user (logout). Accepts strings like "u1". */
//     public void revokeRefreshTokens(String userId) {
//         if (userId == null)
//             return;
//         // Extract digits from the provided identifier (tests use "u1")
//         String digits = userId.replaceAll("\\D+", "");
//         if (digits.isEmpty())
//             return;
//         try {
//             int id = Integer.parseInt(digits);
//             refreshStore.remove(id);
//         } catch (NumberFormatException ignored) {
//         }
//     }

//     /**
//      * Validate a token and return the corresponding Personal if valid and present.
//      */
//     public Personnel validateToken(String token) {
//         try {
//             DecodedJWT jwt = verifier.verify(token);
//             String id = jwt.getSubject();
//             try {
//                 int userId = Integer.parseInt(id);
//                 return database.getPersonnelById(userId);
//             } catch (NumberFormatException nfe) {
//                 return null;
//             }
//         } catch (JWTVerificationException ex) {
//             return null;
//         }
//     }

//     private static class RefreshTokenRecord {
//         final int userId;
//         final String hash; // bcrypt hash of random part
//         final Date expiresAt;

//         RefreshTokenRecord(int userId, String hash, Date expiresAt) {
//             this.userId = userId;
//             this.hash = hash;
//             this.expiresAt = expiresAt;
//         }
//     }

//     public static class AuthResponse {
//         public final String accessToken;
//         public final String refreshToken;
//         public final Date accessExpiresAt;
//         public final Date refreshExpiresAt;
//         public final Personnel user;

//         public AuthResponse(String accessToken, String refreshToken, Date accessExpiresAt, Date refreshExpiresAt,
//                 Personnel user) {
//             this.accessToken = accessToken;
//             this.refreshToken = refreshToken;
//             this.accessExpiresAt = accessExpiresAt;
//             this.refreshExpiresAt = refreshExpiresAt;
//             this.user = user;
//         }
//     }
// }
