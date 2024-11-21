package com.lock.util;

import java.io.File;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public final class DatabaseUtil {

    private static final SessionFactory sessionFactory;
    private static final StandardServiceRegistry registry;


    static {
        registry = buildServiceRegistry();
        sessionFactory = buildSessionFactory();
    }

    // Private constructor to prevent instantiation
    private DatabaseUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Builds the Hibernate ServiceRegistry using dynamic properties.
     * @return StandardServiceRegistry instance.
     */
    private static StandardServiceRegistry buildServiceRegistry() {
        try {
            Properties properties = new Properties();
            properties.setProperty("hibernate.connection.url", "jdbc:h2:file:" + getDBPath() + ";DB_CLOSE_ON_EXIT=TRUE;AUTO_SERVER=TRUE");
            properties.setProperty("hibernate.default_schema", (String) ConfigUtil.getConfigAttribute("database.schema"));

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

    /**
     * Retrieves the global SessionFactory instance.
     * @return the SessionFactory.
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

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

    /**
     * Returns the database file path.
     * @return the database file path as a String.
     */
    private static String getDBPath() {
        return ConfigUtil.getBaseDir() + File.separator + "shdwbx.db";
    }
}
