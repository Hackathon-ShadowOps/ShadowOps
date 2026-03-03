package com.kluster.controller;

import com.kluster.Kluster;

public class AuthServiceTest {
    private Kluster kluster;
    private Database db;

    @org.junit.jupiter.api.BeforeEach
    void setupEnv() throws Exception {
        // Prevent the embedded server from starting during unit tests
        System.setProperty("SKIP_API_RUNNER", "true");

        this.kluster = new Kluster("/home/kactuz/Documents/Github/ShadowOps/kluster/.env"); // Load .env from project root
        this.db = new Database(this.kluster);
    }
}
