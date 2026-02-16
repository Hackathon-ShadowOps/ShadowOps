package com.kluster.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.gson.Gson;

public class Database {
    Gson gson = new Gson();

    private Connection connection = null;

    public Database() {
        try {
            String env = System.getenv("DATABASE_URL");

            if (env == null || env.trim().isEmpty()) {
                System.out.println("\u001B[31mDATABASE_URL not set. Using default SQLite database.\u001B[0m");
                env = "jdbc:sqlite:kluster.db";
            }

            connection = DriverManager.getConnection(env);
        } catch (SQLException e) {
            System.out.print("\u001B[31mFailed to connect to database: \u001B[0m");
            e.printStackTrace(System.err);

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                System.err.println("\u001B[31mFailed to close database connection after failure: \u001B[0m");
                ex.printStackTrace(System.err);
            }

            System.exit(500);
        }

        createAllTables();
    }

    private void createAllTables() {
        String createPersonnelTable = "CREATE TABLE IF NOT EXISTS Personnel (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT," +
            "role INTEGER," +
            "isActive INTEGER," +
            "passwordHash TEXT" +
            ");";

        String createPermissionsTable = "CREATE TABLE IF NOT EXISTS Permissions (" +
            "permissionId INTEGER PRIMARY KEY AUTOINCREMENT," +
            "personnelId INTEGER," +
            "FOREIGN KEY (personnelId) REFERENCES Personnel(id)" +
            ");";

        String createAirplaneTable = "CREATE TABLE IF NOT EXISTS Airplane (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT," +
            "fueltype TEXT," +
            "status INTEGER," +
            "mission TEXT," +
            "longitude REAL," +
            "latitude REAL" +
            ");";

        String createIncidentReportTable = "CREATE TABLE IF NOT EXISTS IncidentReport (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "timestamp INTEGER," +
            "signedBy INTEGER," +
            "description TEXT," +
            "category TEXT," +
            "FOREIGN KEY (signedBy) REFERENCES Personnel(id)" +
            ");";

        String createBaseTable = "CREATE TABLE IF NOT EXISTS Base (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT," +
            "longitude REAL," +
            "latitude REAL," +
            "activityStatus INTEGER," +
            "incidentReport INTEGER," +
            "airportCapacity INTEGER," +
            "airportRunwayLength REAL," +
            "runwayType INTEGER," +
            "airportIsOperational INTEGER," +
            "FOREIGN KEY (incidentReport) REFERENCES IncidentReport(id)" +
            ");";

        String createBaseAssignedAirplaneTable = "CREATE TABLE IF NOT EXISTS BaseAssignedAirplane (" +
            "baseId INTEGER," +
            "airplaneId INTEGER," +
            "FOREIGN KEY (baseId) REFERENCES Base(id)," +
            "FOREIGN KEY (airplaneId) REFERENCES Airplane(id)" +
            ");";

        String createBaseAssignedPersonnelTable = "CREATE TABLE IF NOT EXISTS BaseAssignedPersonnel (" +
            "baseId INTEGER," +
            "personnelId INTEGER," +
            "FOREIGN KEY (baseId) REFERENCES Base(id)," +
            "FOREIGN KEY (personnelId) REFERENCES Personnel(id)" +
            ");";

        /* deliveryType is stored as integer matching DeliveryType enum codes; there is no Delivery table yet */
        String createBaseDeliveryPossibilitiesTable = "CREATE TABLE IF NOT EXISTS BaseDeliveryPossibilities (" +
            "baseId INTEGER," +
            "deliveryType INTEGER," +
            "FOREIGN KEY (baseId) REFERENCES Base(id)" +
            ");";

        // Execute the CREATE TABLE statements in an order that satisfies foreign key dependencies
        String[] stmts = new String[] {
            createPersonnelTable,
            createPermissionsTable,
            createAirplaneTable,
            createIncidentReportTable,
            createBaseTable,
            createBaseAssignedAirplaneTable,
            createBaseAssignedPersonnelTable,
            createBaseDeliveryPossibilitiesTable
        };

        try (Statement stmt = connection.createStatement()) {
            // Ensure SQLite enforces foreign keys
            stmt.execute("PRAGMA foreign_keys = ON;");

            for (String sql : stmts) {
            stmt.execute(sql);
            }
        } catch (SQLException e) {
            System.err.println("Failed to create database tables:");
            e.printStackTrace(System.err);
        }
    }

    // TODO: Implement database operations (CRUD) for Personal, Missions, Logs, etc.
}
