package com.lock;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {

    private final String appName;

    public DatabaseUtil(String appName){
        this.appName = appName;
    }
    
    private String getDatabasePath(){
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home").toLowerCase();
        
        String databaseDir;

        // Find appropriate location for os
        if (os.contains("win")){
            databaseDir = userHome + "\\AppData\\Local\\" + this.appName;
        } else if (os.contains("mac")) {
            databaseDir = userHome + "/Library/Application Support/" + this.appName;
        } else {
            databaseDir = userHome + "/." + this.appName;
        }

        // Create directory if does not exist
        File dir = new File(databaseDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return databaseDir + File.separator + "shdwbx.db";
    }

    public Connection initDatabase(){
        String dbPath = getDatabasePath();
        Connection conn = null;

        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created or opened.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return conn;
    }
}
