package com.lock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

//TODO: Add password and username property dynamiclly to stay secure, take out of cfg.xml

public class DatabaseUtil {

    private SessionFactory sessionFactory;

    public DatabaseUtil() {
        this.sessionFactory = buildSessionFactory();
    }

    private String setupDatabase() {

        String url = "jdbc:mysql://localhost:3306/shdwbx";
        String username = "root";
        String password = "root_admin";

        try {
            // Establishing connection to the database
            Connection connection = DriverManager.getConnection(url, username, password);

            // Creating a statement to execute queries
            Statement statement = connection.createStatement();

            // Example SQL query (you can add more queries here as needed)
            String createTableQuery = "CREATE TABLE IF NOT EXISTS users (" +
                                      "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                      "name VARCHAR(100) NOT NULL, " +
                                      "email VARCHAR(100) NOT NULL" +
                                      ")";

            // Execute the SQL query
            statement.executeUpdate(createTableQuery);
            System.out.println("Database connection successful, table created if it didn't exist.");

            // Clean up and close connections
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }



        return url;
    }

    private Properties getHibernateProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.connection.url", setupDatabase());

        return properties;
    }

    private SessionFactory buildSessionFactory() {
        try {
            // Create the StandardServiceRegistry and apply settings
            StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                //.applySettings(getHibernateProperties())
                .configure()
                .build();

            // Build the Metadata and SessionFactory
            Metadata metadata = new MetadataSources(standardRegistry).getMetadataBuilder().build();
            return metadata.getSessionFactoryBuilder().build();
        } catch (Throwable e) {
            throw new ExceptionInInitializerError("Failed to build SessionFactory: " + e.getMessage());
        }
    }

    @SuppressWarnings("exports")
    public SessionFactory getSessionFactory() {
        return this.sessionFactory;
    }

    public void closeSessionFactory() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
