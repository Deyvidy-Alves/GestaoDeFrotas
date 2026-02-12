package org.example.gestaodefrotas.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoDB {

    private static final String URL = "jdbc:mysql://localhost:3306/frotadb";
    private static final String USER = "root";
    private static final String PASS = "Pf4fkpi0!";

    public static Connection conectar() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            System.out.println("ERRO ao conectar: " + e.getMessage());
            throw e;
        }
    }
}