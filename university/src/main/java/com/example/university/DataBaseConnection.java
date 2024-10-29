package com.example.university;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DataBaseConnection {

    static String username;
    static String password;


    public static Connection connect() {

        String database = "university";
        String SERVER_IP = "jdbc:sqlserver://localhost:1433;databaseName=" + database + ";characterEncoding=UTF8;encrypt=true;" +
                "trustServerCertificate=true;";

        try {
            return DriverManager.getConnection(SERVER_IP, username, password);
        } catch (SQLException ex) {
            throw new RuntimeException("Не удалось установить соединение с БД", ex);
        }
    }
}
