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
    private ConfigUtil configUtil;

    public DatabaseUtil() {
        this.configUtil = new ConfigUtil();
        this.sessionFactory = buildSessionFactory();
    }

    public SessionFactory getSessionFactory() {
        return this.sessionFactory;
    }

    private Properties getHibernateProperties() {
        Properties properties = new Properties();
        String schemaName = (String) configUtil.getConfigAttribute("database.schema");
        properties.setProperty("hibernate.default_schema", schemaName);
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
        return configUtil.getBaseDir() + File.separator + "shdwbx.db";
    }



    public void closeSessionFactory() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
