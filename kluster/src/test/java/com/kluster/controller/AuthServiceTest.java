package com.kluster.controller;

import com.kluster.models.Personal;
import com.kluster.models.PersonalRole;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    @BeforeAll
    static void setupEnv() throws Exception {
        // Set JWT_SECRET in the environment for tests (reflection hack)
        try {
            Map<String, String> env = System.getenv();
            Class<?> cl = env.getClass();
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, String> writableEnv = (Map<String, String>) field.get(env);
            writableEnv.put("JWT_SECRET", "test-secret-123456");
        } catch (NoSuchFieldException e) {
            // Some JVMs may not allow modifying env; fall back to hoping it's set externally
            System.out.println("\u001B[33mWarning: Could not set JWT_SECRET via reflection. Ensure it's set in the environment before running tests.\u001B[0m");
            System.exit(500);
        }
    }

    @Test
    void testAuthenticateAndRefreshFlow() {
        AuthService auth = new AuthService();

        auth.register("u1", "Alice", "Captain", PersonalRole.COMMANDER, "password123");

        AuthService.AuthResponse r = auth.authenticateWithRefresh("u1", "password123", 10, 1);
        assertNotNull(r, "AuthResponse should not be null");
        assertNotNull(r.accessToken, "Access token required");
        assertNotNull(r.refreshToken, "Refresh token required");
        assertNotNull(r.user, "User must be returned");
        assertEquals("u1", r.user.getId());

        Personal validated = auth.validateToken(r.accessToken);
        assertNotNull(validated, "validateToken should return user for valid token");
        assertEquals("u1", validated.getId());

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
        AuthService auth = new AuthService();
        auth.register("u2", "Bob", "Engineer", PersonalRole.ENGINEER, "secret");

        String token = auth.authenticate("u2", "wrong");
        assertNull(token, "authenticate should return null for wrong password");
    }
}
