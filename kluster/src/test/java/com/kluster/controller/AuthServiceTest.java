package com.kluster.controller;

import com.kluster.Kluster;
import com.kluster.models.Personnel;
import com.kluster.models.PersonnelRole;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {
    private Kluster kluster;
    private Database db;
    private AuthService auth;

    @org.junit.jupiter.api.BeforeEach
    void setupEnv() throws Exception {
        // Prevent the embedded server from starting during unit tests
        System.setProperty("SKIP_API_RUNNER", "true");

        this.kluster = new Kluster("/home/kactuz/Documents/Github/ShadowOps/.env"); // Load .env from project root
        this.db = new Database(this.kluster);
        this.auth = new AuthService(this.db, this.kluster);
    }

    @Test
    void testAuthenticateAndRefreshFlow() {
        auth.register(1, "Alice", 1, PersonnelRole.COMMANDER, "password123", 0);

        AuthService.AuthResponse r = auth.authenticateWithRefresh(1, "password123", 10, 1);
        assertNotNull(r, "AuthResponse should not be null");
        assertNotNull(r.accessToken, "Access token required");
        assertNotNull(r.refreshToken, "Refresh token required");
        assertNotNull(r.user, "User must be returned");
        assertEquals(1, r.user.getId());

        Personnel validated = auth.validateToken(r.accessToken);
        assertNotNull(validated, "validateToken should return user for valid token");
        assertEquals(1, validated.getId());

        // Use refresh token to get new pair
        AuthService.AuthResponse r2 = auth.refreshWithToken(r.refreshToken, 10, 1);
        assertNotNull(r2, "refreshWithToken should return new tokens");
        assertNotEquals(r.refreshToken, r2.refreshToken, "Refresh token should be rotated");

        // Old refresh token should now be invalid
        AuthService.AuthResponse r3 = auth.refreshWithToken(r.refreshToken, 10, 1);
        assertNull(r3, "Old refresh token must be invalid after rotation");

        // Revoke tokens and ensure refresh no longer works
        auth.revokeRefreshTokens("u1");
        AuthService.AuthResponse r4 = auth.refreshWithToken(r2.refreshToken, 10, 1);
        assertNull(r4, "Refresh token should be invalid after revoke");
    }

    @Test
    void testAuthenticateInvalidPassword() {
        auth.register(2, "Bob", 2, PersonnelRole.ENGINEER, "secret", 0);

        String token = auth.authenticate(2, "wrong");
        assertNull(token, "authenticate should return null for wrong password");
    }

    @Test
    void testTamperedAccessTokenIsInvalid() {
        AuthService auth = new AuthService(db, kluster);
        auth.register(3, "Charlie", 3, PersonnelRole.PILOT, "pw", 0);

        AuthService.AuthResponse r = auth.authenticateWithRefresh(3, "pw", 10, 1);
        assertNotNull(r, "auth response should not be null");

        // simple tamper by appending characters
        String tampered = r.accessToken + "x";
        Personnel p = auth.validateToken(tampered);
        assertNull(p, "tampered access token should be invalid");
    }

    @Test
    void testDifferentSecretTokenIsInvalid() {
        auth.register(5, "Echo", 5, PersonnelRole.COMMANDER, "pw3", 0);

        // create token with a different secret so signature won't match
        Algorithm badAlg = Algorithm.HMAC256("bad-secret-000");
        String bad = JWT.create()
                .withSubject("5")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 60000))
                .sign(badAlg);

        Personnel p = auth.validateToken(bad);
        assertNull(p, "token signed with different secret should be invalid");
    }

    @Test
    void testTamperedRefreshTokenIsRejected() {
        auth.register(6, "Foxtrot", 6, PersonnelRole.ENGINEER, "pw4", 0);

        AuthService.AuthResponse r = auth.authenticateWithRefresh(6, "pw4", 10, 1);
        assertNotNull(r, "auth response should not be null");

        // mutate the opaque refresh token
        String badRefresh = r.refreshToken.substring(0, r.refreshToken.length() - 1) + 'Z';
        AuthService.AuthResponse rr = auth.refreshWithToken(badRefresh, 10, 1);
        assertNull(rr, "tampered refresh token should be rejected");
    }

    @Test
    void testRoleAuthorizationAllowedAndDenied() {
        // Commander should be allowed to perform commander-only action
        auth.register(10, "Leader", 10, PersonnelRole.COMMANDER, "leadpw", 0);
        auth.register(11, "Worker", 11, PersonnelRole.ENGINEER, "workpw", 0);

        AuthService.AuthResponse a1 = auth.authenticateWithRefresh(10, "leadpw", 10, 1);
        AuthService.AuthResponse a2 = auth.authenticateWithRefresh(11, "workpw", 10, 1);

        Personnel p1 = auth.validateToken(a1.accessToken);
        Personnel p2 = auth.validateToken(a2.accessToken);

        assertNotNull(p1);
        assertNotNull(p2);

        // simple permission check used by endpoints: only COMMANDER allowed
        java.util.function.Predicate<Personnel> isCommander = u -> u != null && u.getRole() == PersonnelRole.COMMANDER;

        assertTrue(isCommander.test(p1), "Commander must be allowed");
        assertFalse(isCommander.test(p2), "Engineer must be denied commander-only action");
    }

    @Test
    void testInvalidAccessTokenRequiresReauth() {
        auth.register(20, "Gamma", 20, PersonnelRole.PILOT, "pass", 0);

        AuthService.AuthResponse r = auth.authenticateWithRefresh(20, "pass", 10, 1);
        assertNotNull(r);

        // tamper access token -> validation fails
        Personnel p = auth.validateToken(r.accessToken + "tamper");
        assertNull(p, "Tampered token must not validate");

        // re-authenticate to get a fresh token
        String newToken = auth.authenticate(20, "pass");
        assertNotNull(newToken, "Re-authentication must return a token");
        Personnel p2 = auth.validateToken(newToken);
        assertNotNull(p2, "New token must validate and allow actions");
    }
}
