package com.kluster.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.kluster.Kluster;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AirplaneScheduleTest {
    private Kluster klusterMock;
    private Dotenv dotenvMock;
    private Database db;

    @BeforeEach
    void setup() throws Exception {
        this.klusterMock = Mockito.mock(Kluster.class);
        this.dotenvMock = Mockito.mock(Dotenv.class);

        Mockito.when(klusterMock.env()).thenReturn(this.dotenvMock);
        // Use an in-memory SQLite instance for isolated tests
        Mockito.when(this.dotenvMock.get("DATABASE_URL")).thenReturn("jdbc:sqlite::memory:");

        this.db = new Database(this.klusterMock);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (this.db != null) {
            this.db.close();
        }
    }

    private Connection getConnectionFromDb() throws Exception {
        Field connField = Database.class.getDeclaredField("connection");
        connField.setAccessible(true);
        return (Connection) connField.get(this.db);
    }

    @Test
    void testAddAirplaneSchedule() throws Exception {
        String airplaneId = "ADD-1";
        int groundSpace = 5;
        long start = 1000L;
        long end = 1100L;

        boolean ok = this.db.updateAirplaneSchedule(airplaneId, groundSpace, start, end);
        assertTrue(ok);

        Connection conn = getConnectionFromDb();
        try (PreparedStatement ps = conn.prepareStatement("SELECT airplane_id, ground_space, start_time, end_time FROM airplane_schedule WHERE airplane_id = ?")) {
            ps.setString(1, airplaneId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(airplaneId, rs.getString("airplane_id"));
                assertEquals(groundSpace, rs.getInt("ground_space"));
                assertEquals(start, rs.getLong("start_time"));
                assertEquals(end, rs.getLong("end_time"));
            }
        }
    }

    @Test
    void testChangeTimeForAirplane() throws Exception {
        String airplaneId = "CHG-1";
        int groundSpace = 3;
        long start1 = 2000L;
        long end1 = 2100L;
        long start2 = 3000L;
        long end2 = 3100L;

        assertTrue(this.db.updateAirplaneSchedule(airplaneId, groundSpace, start1, end1));

        // Update with new times
        assertTrue(this.db.updateAirplaneSchedule(airplaneId, groundSpace, start2, end2));

        Connection conn = getConnectionFromDb();
        try (PreparedStatement ps = conn.prepareStatement("SELECT start_time, end_time FROM airplane_schedule WHERE airplane_id = ?")) {
            ps.setString(1, airplaneId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(start2, rs.getLong("start_time"));
                assertEquals(end2, rs.getLong("end_time"));
            }
        }
    }

    @Test
    void testRemoveAirplaneSchedule() throws Exception {
        String airplaneId = "DEL-1";
        int groundSpace = 2;
        long start = 4000L;
        long end = 4100L;

        assertTrue(this.db.updateAirplaneSchedule(airplaneId, groundSpace, start, end));

        // Ensure present
        Connection conn = getConnectionFromDb();
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS c FROM airplane_schedule WHERE airplane_id = ?")) {
            ps.setString(1, airplaneId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt("c"));
            }
        }

        // Remove
        assertTrue(this.db.removeAirplaneSchedule(airplaneId));

        conn = getConnectionFromDb();
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS c FROM airplane_schedule WHERE airplane_id = ?")) {
            ps.setString(1, airplaneId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt("c"));
            }
        }

        // Removing again should return false
        assertFalse(this.db.removeAirplaneSchedule(airplaneId));
    }
}
