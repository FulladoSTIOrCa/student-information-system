/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sis;

/**
 *
 * @author Jason
 */
import java.sql.Connection;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {
        Connection conn = DBConnection.connect();
        if (conn != null) {
            System.out.println("✅ CONNECTED SUCCESSFULLY");
            try {
                System.out.println("📄 DB Product: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("🔢 DB Version: " + conn.getMetaData().getDatabaseProductVersion());
                conn.close();
                System.out.println("🔒 Connection closed cleanly.");
            } catch (SQLException e) {
                System.out.println("⚠️ Connected but error reading metadata.");
                e.printStackTrace();
            }
        } else {
            System.out.println("❌ CONNECTION FAILED");
        }
    }
}
