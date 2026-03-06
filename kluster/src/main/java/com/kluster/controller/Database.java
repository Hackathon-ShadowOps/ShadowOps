package com.kluster.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.kluster.Kluster;
import com.kluster.models.Airplane;
import com.kluster.models.AirplaneSchedules;

public class Database {
    private Gson gson = new Gson();
    private final Kluster kluster;
    private Connection connection = null;

    public enum Tables {
        PERSONNEL("personnel"),
        BASE("base"),
        AIRPLANE("airplane"),
        LOG("log");

        private final String tableName;

        Tables(String tableName) {
            this.tableName = tableName;
        }

        public String getTableName() {
            return tableName;
        }
    }

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
        ArrayList<String> stmts = new ArrayList<>();

        for (Tables table : Tables.values()) {
            stmts.add(String.format("CREATE TABLE IF NOT EXISTS %s ( data TEXT NOT NULL );", table.getTableName()));
        }

        try (Statement stmt = connection.createStatement()) {
            for (String sql : stmts) {
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            System.err.println("Failed to create database tables:");
            e.printStackTrace(System.err);
        }

        String createAirplaneScheduleTable = "CREATE TABLE IF NOT EXISTS airplane_schedule (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "airplane_id TEXT NOT NULL UNIQUE," +
                "ground_space INTEGER NOT NULL," +
                "start_time long NOT NULL," +
                "end_time long NOT NULL" +
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createAirplaneScheduleTable);
        } catch (SQLException e) {
            System.err.println("Failed to create airplane schedule table:");
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

    /**
     * Generic method to insert data into a specified table. The data is converted
     * to a JSON string before being stored in the database.
     * 
     * @param table The table to insert data into
     * @param data  The Java object to be inserted, which will be converted to JSON
     * @throws SQLException
     */
    public void insertData(Tables table, Object data) throws SQLException {
        String jsonData = convertToJson(data);
        String sql = String.format("INSERT INTO %s (data) VALUES (?);", table.getTableName());

        try (var pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, jsonData);
            pstmt.executeUpdate();
        }
    }

    public void insertData(Tables table, ArrayList<Object> data) throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Database connection is not available");
        }

        String sql = String.format("INSERT INTO %s (data) VALUES (?);", table.getTableName());

        boolean previousAutoCommit = connection.getAutoCommit();
        startTransaction();

        try (var pstmt = connection.prepareStatement(sql)) {
            for (Object obj : data) {
                String jsonData = convertToJson(obj);
                pstmt.setString(1, jsonData);
                pstmt.executeUpdate();
            }

            commitTransaction();
        } catch (SQLException e) {
            try {
                rollbackTransaction();
            } catch (SQLException rbEx) {
                e.addSuppressed(rbEx);
            }
            throw e;
        } finally {
            try {
                if (connection != null && !connection.getAutoCommit()) {
                    connection.setAutoCommit(previousAutoCommit);
                }
            } catch (SQLException ex) {
                // If we cannot restore auto-commit, surface that as a SQLException
                throw ex;
            }
        }
    }

    /**
     * Generic method to retrieve all records from a specified table and convert
     * them. Uses the provided class type to convert JSON strings back into Java
     * objects.
     * 
     * @param <T>   The type of objects to be returned in the list, which should
     *              match the class type used when inserting data into the table
     * @param table The table to retrieve data from
     * @param clazz The class type to use for converting JSON strings back into Java
     *              objects
     * @return An ArrayList of Java objects of the specified type, representing all
     *         records retrieved from the specified table
     * @throws SQLException
     */
    public <T> ArrayList<T> getAllData(Tables table, Class<T> clazz) throws SQLException {
        String sql = String.format("SELECT data FROM %s;", table.getTableName());
        ArrayList<T> results = new ArrayList<>();

        try (var stmt = connection.createStatement();
                var rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String jsonData = rs.getString("data");
                T obj = convertFromJson(jsonData, clazz);
                results.add(obj);
            }
        }

        return results;
    }

    /**
     * Utility methods for converting between Java objects and JSON strings using
     * Gson.
     * 
     * @param data The Java object to be converted to a JSON string
     * @return A JSON string representation of the provided Java object
     */
    public String convertToJson(Object data) {
        return gson.toJson(data);
    }

    /**
     * Generic method to convert a JSON string back into a Java object of the
     * specified class type.
     * 
     * @param <T>   The type of the Java object to be returned, which should match
     *              the class type
     * @param json  The JSON string to be converted back into a Java object
     * @param clazz The class type to use for converting the JSON string back into a
     *              Java object
     * @return A Java object of the specified type, created by converting the
     *         provided JSON string
     */
    public <T> T convertFromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public ArrayList<AirplaneSchedules> getAirplaneLandingSchedule(long startTime, long endTime) {
        ArrayList<AirplaneSchedules> schedules = new ArrayList<AirplaneSchedules>();

        // Retrieve all airplane schedules from the database
        String sql = "SELECT id, airplane_id, ground_space, start_time, end_time FROM airplane_schedule " +
                "WHERE (start_time < ? AND end_time > ?) OR (start_time >= ? AND start_time < ?) OR (end_time > ? AND end_time <= ?);";

        try (
                var pstmt = connection.prepareStatement(sql);
        ) {
            pstmt.setLong(1, startTime);
            pstmt.setLong(2, startTime);
            pstmt.setLong(3, startTime);
            pstmt.setLong(4, endTime);
            pstmt.setLong(5, startTime);
            pstmt.setLong(6, endTime);

            try (var rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int databaseId = rs.getInt("id");
                String airplaneId = rs.getString("airplane_id");
                int groundSpace = rs.getInt("ground_space");
                long scheduleStartTime = rs.getLong("start_time");
                long scheduleEndTime = rs.getLong("end_time");

                AirplaneSchedules schedule = new AirplaneSchedules(databaseId, airplaneId, groundSpace,
                        scheduleStartTime, scheduleEndTime);
                schedules.add(schedule);
            }
            }
        } catch (Exception e) {
            System.err.println("Failed to retrieve airplane landing schedules:");
            e.printStackTrace(System.err);
            return null;
        }

        return schedules;
    }

    /**
     * Updates the landing schedule for a specific airplane in the database. This
     * method
     * 
     * @param airplaneId
     * @param groundSpace The new ground space the airplane will occupy during its
     *                    landing schedule
     * @param startTime   The new start time for the airplane's landing schedule,
     *                    represented as a Unix timestamp in minutes
     * @param endTime     The new end time for the airplane's landing schedule,
     *                    represented as a Unix timestamp in minutes
     * @return
     */
    public boolean updateAirplaneSchedule(int scheduleId, int groundSpace, long startTime, long endTime) {
        String sql = "UPDATE airplane_schedule SET ground_space = ?, start_time = ?, end_time = ? WHERE id = ?;";

        try (var pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, groundSpace);
            pstmt.setLong(2, startTime);
            pstmt.setLong(3, endTime);
            pstmt.setInt(4, scheduleId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (Exception e) {
            System.err.println("Failed to update airplane schedule:");
            e.printStackTrace(System.err);
            return false;
        }
    }

    /**
     * Removes an airplane schedule from the database based on the provided airplane
     * ID.
     * 
     * @param airplaneId The ID of the airplane whose schedule should be removed
     * @return true if the schedule was successfully removed, false otherwise
     */
    public boolean removeAirplaneSchedule(String airplaneId) {
        // Remove the airplane schedule from the database based on the provided airplane
        // ID

        String sql = "DELETE FROM airplane_schedule WHERE airplane_id = ?";

        try (var pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, airplaneId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (Exception e) {
            System.err.println("Failed to remove airplane schedule:");
            e.printStackTrace(System.err);
        }

        return false;
    }

    public boolean addAirplaneSchedule(String airplaneId, long startTime, long endTime) {
        // Add a new airplane schedule to the database with default values
        String sql = "INSERT INTO airplane_schedule (airplane_id, ground_space, start_time, end_time) " +
                "VALUES (?, 0, ?, ?);";

        try (var pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, airplaneId);

            System.out.println("Adding airplane schedule with start time: " + startTime + "\nand end time: " + endTime);

            // Parse strings to long integers before setting
            long startTimeLong = startTime;
            long endTimeLong = endTime;

            pstmt.setLong(2, startTimeLong);
            pstmt.setLong(3, endTimeLong);
            pstmt.executeUpdate();
            return true;
        } catch (NumberFormatException e) {
            System.err.println("Invalid time format - must be a valid number:");
            e.printStackTrace(System.err);
            return false;
        } catch (Exception e) {
            System.err.println("Failed to add airplane schedule:");
            e.printStackTrace(System.err);
            return false;
        }
    }

}
