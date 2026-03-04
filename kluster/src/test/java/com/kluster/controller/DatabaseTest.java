package com.kluster.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import com.kluster.Kluster;
import com.kluster.models.TestModel;
import com.kluster.models.Airplane;
import com.kluster.models.Base;
import com.kluster.models.GpsCords;
import com.kluster.models.IncidentReport;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DatabaseTest {
    private Kluster klusterMock;
    private Dotenv dotenvMock;
    private Database db;

    @BeforeEach
    void setup() throws Exception {
        // Mock Kluster and Dotenv so we can supply an in-memory SQLite URL
        this.klusterMock = Mockito.mock(Kluster.class);
        this.dotenvMock = Mockito.mock(Dotenv.class);

        Mockito.when(klusterMock.env()).thenReturn(this.dotenvMock);
        Mockito.when(this.dotenvMock.get("DATABASE_URL")).thenReturn("jdbc:sqlite::memory:");

        this.db = new Database(this.klusterMock);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (this.db != null) {
            this.db.close();
        }
    }

    @Test
    void testSingleInsertAndRetrieve() throws Exception {
        TestModel t = new TestModel(1, "Alice", true);

        this.db.insertData(Database.Tables.PERSONNEL, t);

        List<TestModel> all = this.db.getAllData(Database.Tables.PERSONNEL, TestModel.class);
        assertEquals(1, all.size());
        assertEquals("Alice", all.get(0).getName());
        assertEquals(1, all.get(0).getId());
    }

    @Test
    void testBatchInsertAndRetrieve() throws Exception {
        ArrayList<Object> batch = new ArrayList<>();
        batch.add(new TestModel(2, "Bob", true));
        batch.add(new TestModel(3, "Carol", false));

        this.db.insertData(Database.Tables.PERSONNEL, batch);
        List<TestModel> all = this.db.getAllData(Database.Tables.PERSONNEL, TestModel.class);
        assertEquals(2, all.size());

        // Names may come back in insertion order
        List<String> names = List.of(all.get(0).getName(), all.get(1).getName());
        // Ensure both expected names present
        assertEquals(true, names.contains("Bob"));
        assertEquals(true, names.contains("Carol"));
    }

    @Test
    void testFourPerTable() throws Exception {
        ArrayList<Object> personnelBatch = new ArrayList<>();

        for (int i = 1; i <= 4; i++) {
            personnelBatch.add(new TestModel(i, "Person" + i, i % 2 == 0));
        }

        this.db.insertData(Database.Tables.PERSONNEL, personnelBatch);
        List<TestModel> personnel = this.db.getAllData(Database.Tables.PERSONNEL, TestModel.class);

        assertEquals(4, personnel.size());

        for (int i = 1; i <= 4; i++) {
            assertEquals(i, personnel.get(i - 1).getId());
            assertEquals("Person" + i, personnel.get(i - 1).getName());
            assertEquals(i % 2 == 0, personnel.get(i - 1).isActive());
        }
    }
}
