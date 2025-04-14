package edu.hei.school.restaurant.dao;

import edu.hei.school.restaurant.service.exception.ServerException;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class DataSource {
    private final Dotenv dotenv = Dotenv.load(); // Charger dotenv
    private final String host = dotenv.get("DATABASE_HOST");
    private final String port = dotenv.get("DATABASE_PORT", "5432"); // Valeur par défaut
    private final String user = dotenv.get("DATABASE_USER");
    private final String password = dotenv.get("DATABASE_PASSWORD");
    private final String database = dotenv.get("DATABASE_NAME");
    private final String jdbcUrl;

    public DataSource() {
        jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        System.out.println("JDBC URL: " + jdbcUrl);
        System.out.println("User: " + user);
        System.out.println("Host: " + host);
        System.out.println("Database: " + database);
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(jdbcUrl, user, password);
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }
}
