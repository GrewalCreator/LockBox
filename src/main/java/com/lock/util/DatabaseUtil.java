package com.lock.util;

import java.io.File;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

//TODO: Add password and username property dynamiclly to stay secure, take out of cfg.xml

public class DatabaseUtil {

    private SessionFactory sessionFactory;
    private final String APP_NAME = ConfigUtil.getAppName();

    public DatabaseUtil() {
        this.sessionFactory = buildSessionFactory();
    }

    public SessionFactory getSessionFactory() {
        return this.sessionFactory;
    }

    private Properties getHibernateProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.connection.url", "jdbc:h2:file:" + getDBPath() + ";DB_CLOSE_ON_EXIT=TRUE;AUTO_SERVER=TRUE");

        return properties;
    }

    private SessionFactory buildSessionFactory() {
        try {
            // Create the StandardServiceRegistry and apply settings
            StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                .applySettings(getHibernateProperties())
                .configure()
                .build();

            // Build the Metadata and SessionFactory
            Metadata metadata = new MetadataSources(standardRegistry).getMetadataBuilder().build();

            return metadata.getSessionFactoryBuilder().build();
        } catch (Throwable e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError("Failed to build SessionFactory: " + e.getMessage());
        }
    }

    private String getDBPath(){
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home").toLowerCase();

        String databaseDir;
        // Find appropriate location for os
        if (os.contains("win")){
            databaseDir = userHome + "\\AppData\\Local\\" + this.APP_NAME;
        } else if (os.contains("mac")) {
            databaseDir = userHome + "/Library/Application Support/" + this.APP_NAME;
        } else {
            databaseDir = userHome + "/." + this.APP_NAME;
        }
        // Create directory if does not exist
        File dir = new File(databaseDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return databaseDir + File.separator + "shdwbx.db";
    }



    public void closeSessionFactory() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
