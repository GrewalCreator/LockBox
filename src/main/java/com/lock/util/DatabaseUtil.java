package com.lock.util;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class DatabaseUtil {

    private static final SessionFactory sessionFactory;
    private static final StandardServiceRegistry registry;
    private static String databasePath;
        private static final String DATABASE_NAME = "shdwbx";
        private static HikariDataSource dataSource;

        static {
            initDatabase();
            registry = buildServiceRegistry();
            sessionFactory = buildSessionFactory();
        }

        private DatabaseUtil() {
            throw new UnsupportedOperationException("Utility class");
        }

        /**
         * Builds the Hibernate ServiceRegistry using dynamic properties.
         * @return StandardServiceRegistry instance.
         */
        private static StandardServiceRegistry buildServiceRegistry() {
            try {
                String username = (String) ConfigUtil.getConfigAttribute("database.username");
                String password = (String) ConfigUtil.getConfigAttribute("database.password");

                Properties properties = new Properties();
                properties.setProperty("hibernate.connection.url", getJDBCPath() + ";DB_CLOSE_ON_EXIT=TRUE;AUTO_SERVER=TRUE");
                properties.setProperty("hibernate.default_schema", (String) ConfigUtil.getConfigAttribute("database.schema"));
                properties.put("hibernate.hikari.dataSource", getDataSource());

                properties.setProperty("hibernate.connection.username", username);
                properties.setProperty("hibernate.connection.password", password);


                return new StandardServiceRegistryBuilder()
                    .applySettings(properties)
                    .configure()
                    .build();

            } catch (Throwable e) {
                throw new ExceptionInInitializerError("Failed to initialize Hibernate ServiceRegistry: " + e.getMessage());
            }
        }

        /**
         * Builds the Hibernate SessionFactory using the ServiceRegistry.
         * @return SessionFactory instance.
         */
        private static SessionFactory buildSessionFactory() {
            Metadata metadata = new MetadataSources(registry).getMetadataBuilder().build();
            return metadata.getSessionFactoryBuilder().build();
        }

        private static String initDatabase(){
            String dbPath = ConfigUtil.getBaseDir() + File.separator + DATABASE_NAME + ".db";
            databasePath = dbPath;

        // Setup Schema & User
        String username = ConfigUtil.getAppName() + "_" + System.getenv("USER");
        String password = SecureUtil.generateRandomPassword(15);
        String schemaName = (String) ConfigUtil.getConfigAttribute("database.schema");

        ConfigUtil.setConfigAttribute("database.username", username);
        ConfigUtil.setConfigAttribute("database.password", password);


        initConnectionPool("sa", "");

        try (Connection conn = getDataSource().getConnection()) {
            Statement query = conn.createStatement();
            query.executeUpdate(String.format("CREATE SCHEMA IF NOT EXISTS %s;", schemaName));
            query.executeUpdate(String.format("CREATE USER IF NOT EXISTS %s PASSWORD '%s';", username, password));
            query.executeUpdate(String.format("GRANT ALL PRIVILEGES ON SCHEMA %s TO %s;", schemaName, username));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dbPath;
    }

    private static void initConnectionPool(String username, String password){
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(getJDBCPath());
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize((Integer) ConfigUtil.getConfigAttribute("database.connection.poolSize"));

        // seconds to milliseconds
        config.setIdleTimeout((Integer) ConfigUtil.getConfigAttribute("database.connection.timeout") * 1_000);
        config.setConnectionTimeout((Integer) ConfigUtil.getConfigAttribute("database.connection.connectionTimeout") * 1_000);

        // minutes to miliseconds
        config.setMaxLifetime((Integer) ConfigUtil.getConfigAttribute("database.connection.lifetime") * 60 * 1_000);


        dataSource = new HikariDataSource(config);
    }

    public static HikariDataSource getDataSource(){return dataSource;}
    public static SessionFactory getSessionFactory() {return sessionFactory;}

    public static String getDBPath() {
        return databasePath;
    }

    public static String getJDBCPath(){
        return "jdbc:h2:file:" + getDBPath();
    }

    public static String getDBName(){return DATABASE_NAME;}

    /**
     * Closes the SessionFactory and associated resources.
     */
    public static void closeSessionFactory() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
