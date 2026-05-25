/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import io.github.cdimascio.dotenv.Dotenv;

public class DBConnection {

    public static Connection connect() {
        Dotenv dotenv = Dotenv.load();

        // Local variables are fine here
        String url = dotenv.get("DB_URL");
        String user = dotenv.get("DB_USER");
        String pass = dotenv.get("DB_PASS");

        try {
            // Class.forName is usually not required for modern JDBC drivers
            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("DB Connected!");
            return conn;
        } catch (SQLException e) {
            // Catching specific SQL exceptions is better than generic 'Exception'
            System.err.println("Connection failed: " + e.getMessage());
            return null; 
        }
    }
}