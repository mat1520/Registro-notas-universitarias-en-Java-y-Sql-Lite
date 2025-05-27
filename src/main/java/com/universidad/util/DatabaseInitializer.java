package com.universidad.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {
    
    public static void initialize() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(
                     DatabaseInitializer.class.getResourceAsStream("/sql/init.sql")))) {
            
            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sql.append(line).append("\n");
            }
            
            // Ejecutar el script SQL
            for (String command : sql.toString().split(";")) {
                if (!command.trim().isEmpty()) {
                    stmt.execute(command);
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Error initializing database", e);
        }
    }
} 