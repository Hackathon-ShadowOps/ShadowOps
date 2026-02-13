package com.kluster.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.google.gson.Gson;

public class Database {
    Gson gson = new Gson();

    private Connection connection = null;

    public Database() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:database.db");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.print("Failed to connect to database!");
        }

        createAllTables();
    }

    private void createAllTables() {
    }
}
