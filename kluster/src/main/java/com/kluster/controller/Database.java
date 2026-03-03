package com.kluster.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.kluster.Kluster;
import com.kluster.models.Personnel;
import com.kluster.models.PersonnelRole;

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
        ArrayList<String> stmts = new ArrayList<>();

        stmts.add("CREATE TABLE IF NOT EXISTS personnel ( data TEXT NOT NULL );");
        stmts.add("CREATE TABLE IF NOT EXISTS base ( data TEXT NOT NULL );");
        stmts.add("CREATE TABLE IF NOT EXISTS airplane ( data TEXT NOT NULL );");
        stmts.add("CREATE TABLE IF NOT EXISTS log ( data TEXT NOT NULL );");

        try (Statement stmt = connection.createStatement()) {
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

    /**
     * Add methods to interact with the database here, e.g.:
     * - addPersonnel(Personnel p)
     * - getPersonnelById(String id)
     * - updatePersonnel(Personnel p)
     * - deletePersonnel(String id)
     */
}
