package com.kluster.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.kluster.Kluster;

public class Database {
    private Gson gson = new Gson();
    private final Kluster kluster;
    private Connection connection = null;

    public Database(Kluster kluster) {
        this.kluster = kluster;

        try {
            String env = kluster.env().get("DATABASE_URL");

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

        /*
         * deliveryType is stored as an integer matching DeliveryType enum codes; there
         * is no Delivery table yet
         */
        String createBaseDeliveryPossibilitiesTable = "CREATE TABLE IF NOT EXISTS BaseDeliveryPossibilities (" +
                "baseId INTEGER," +
                "deliveryType INTEGER," +
                "FOREIGN KEY (baseId) REFERENCES Base(id)" +
                ");";

        String createLogTable = "CREATE TABLE IF NOT EXISTS Log (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "timestamp INTEGER," +
                "personnelId INTEGER," +
                "action TEXT," +
                "details TEXT," +
                "FOREIGN KEY (personnelId) REFERENCES Personnel(id)" +
                ");";

        // Execute the CREATE TABLE statements in an order that satisfies foreign key
        // dependencies
        String[] stmts = new String[] {
                createPersonnelTable,
                createPermissionsTable,
                createAirplaneTable,
                createIncidentReportTable,
                createBaseTable,
                createBaseAssignedAirplaneTable,
                createBaseAssignedPersonnelTable,
                createBaseDeliveryPossibilitiesTable,
                createLogTable
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

    public void startTransaction() throws SQLException {
        connection.setAutoCommit(false);
    }

    public void commitTransaction() throws SQLException {
        connection.commit();
        connection.setAutoCommit(true);
    }

    public void rollbackTransaction() throws SQLException {
        connection.rollback();
        connection.setAutoCommit(true);
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    private String logUsage(int personnelId, String action, String details) throws SQLException {
        return String.format(
                "INSERT INTO Log (timestamp, personnelId, action, details) VALUES (%d, %d, '%s', '%s');",
                System.currentTimeMillis(),
                personnelId,
                action.replace("'", "''"),
                details.replace("'", "''"));
    }

    public synchronized void executeUpdate(String sql) throws SQLException {
        ArrayList<String> sqlList = new ArrayList<>();
        sqlList.add(sql);

        executeUpdate(sqlList);
    }

    public synchronized void executeUpdate(ArrayList<String> sqlList) throws SQLException {
        startTransaction();

        try (Statement stmt = connection.createStatement()) {
            for (String sql : sqlList) {
                stmt.executeUpdate(sql);
            }
        } catch (SQLException e) {
            try {
                rollbackTransaction();
            } catch (SQLException rbEx) {
                e.addSuppressed(rbEx);
                System.err.println("Rollback failed after update error: " + rbEx.getMessage());
            }

            System.err.println("Database update failed for SQL list");
            throw e;
        }

        try {
            commitTransaction();
        } catch (SQLException e) {
            try {
                rollbackTransaction();
            } catch (SQLException rbEx) {
                e.addSuppressed(rbEx);
                System.err.println("Rollback failed after commit error: " + rbEx.getMessage());
            }

            System.err.println("Database commit failed for SQL list");
            throw e;
        } finally {
            try {
                if (connection != null && !connection.getAutoCommit()) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                System.err.println("Failed to reset auto-commit: " + ex.getMessage());
            }
        }
    }

    public void addPersonnel(String name, int role, boolean isActive, String passwordHash, int signedByPersonnelId)
            throws SQLException {
        ArrayList<String> sqlList = new ArrayList<>();

        sqlList.add(String.format(
                "INSERT INTO Personnel (name, role, isActive, passwordHash) VALUES ('%s', %d, %d, '%s');",
                name.replace("'", "''"),
                role,
                isActive ? 1 : 0,
                passwordHash.replace("'", "''")));

        sqlList.add(logUsage(signedByPersonnelId, "addPersonnel", name));

        executeUpdate(sqlList);
    }

    public void addBase(String name, double longitude, double latitude, int activityStatus, int incidentReport,
            int airportCapacity, double airportRunwayLength, int runwayType, boolean airportIsOperational,
            int signedByPersonnelId)
            throws SQLException {
        ArrayList<String> sqlList = new ArrayList<>();
        sqlList.add(String.format(
                "INSERT INTO Base (name, longitude, latitude, activityStatus, incidentReport, airportCapacity, airportRunwayLength, runwayType, airportIsOperational) "
                        +
                        "VALUES ('%s', %f, %f, %d, %d, %d, %f, %d, %d);",
                name.replace("'", "''"),
                longitude,
                latitude,
                activityStatus,
                incidentReport,
                airportCapacity,
                airportRunwayLength,
                runwayType,
                airportIsOperational ? 1 : 0));

        sqlList.add(logUsage(signedByPersonnelId, "addBase", name));

        executeUpdate(sqlList);
    }

    public void addAirplane(String name, String fuelType, int status, String mission, double longitude, double latitude,
            int signedByPersonnelId)
            throws SQLException {
        ArrayList<String> sqlList = new ArrayList<>();
        sqlList.add(String.format(
                "INSERT INTO Airplane (name, fueltype, status, mission, longitude, latitude) " +
                        "VALUES ('%s', '%s', %d, '%s', %f, %f);",
                name.replace("'", "''"), // Escape single quotes in name
                fuelType.replace("'", "''"), // Escape single quotes in fuelType
                status,
                mission.replace("'", "''"), // Escape single quotes in mission
                longitude,
                latitude));

        sqlList.add(logUsage(signedByPersonnelId, "addAirplane", name));

        executeUpdate(sqlList);
    }

    // TODO: Implement database operations (CRUD) for Personnel, Missions, Logs,
    // etc.
}
