package com.lock.util;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigUtil {

    private static final String CONFIG_FILE_TEMPLATE = "/com/lock/resources/config_template.yaml";
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static String configPath;
    private String baseDir;

    public ConfigUtil() {
        setupConfig();
    }

    /**
     * Sets up the configuration directory and config file.
     */
    private void setupConfig() {
        String userHome = System.getProperty("user.home");
        String appName = loadConfig(CONFIG_FILE_TEMPLATE).get("appName");

        // Base directory for the app based on the OS
        if (OS.contains("win")) {
            this.baseDir = userHome + "\\AppData\\Local\\" + appName;
        } else if (OS.contains("mac")) {
            this.baseDir = userHome + "/Library/Application Support/" + appName;
        } else {
            this.baseDir = userHome + "/." + appName;
        }

        // Resources directory
        File resourcesDir = new File(this.baseDir, "resources");
        if (!resourcesDir.exists() && resourcesDir.mkdirs()) {
            System.out.println("Created directory: " + resourcesDir.getAbsolutePath());
        }


        configPath = new File(resourcesDir, "config.yaml").getAbsolutePath();


        initConfigFile(appName);

    }


    public String getAppName() {
        Map<String, String> configMap = loadConfig(configPath);
        return configMap.get("appName");
    }

    public String getBaseDir(){
        return this.baseDir;
    }

    public static String getConfigPath() {
        return configPath;
    }

/**
 * Loads the configuration from a resource file accessible via the classpath.
 * @param resourcePath the path to the configuration file in the classpath.
 * @return a map of configuration key-value pairs.
 */
private static Map<String, String> loadConfig(String resourcePath) {
    Map<String, String> configMap = new HashMap<>();

    try (InputStream inputStream = ConfigUtil.class.getResourceAsStream(resourcePath)) {
        if (inputStream == null) {
            throw new FileNotFoundException("Resource not found: " + resourcePath);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            // Split line by the first colon to get key-value pairs
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim();

                // Remove surrounding quotes, if any
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }

                configMap.put(key, value);
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

    return configMap;
}


 /**
 * Initializes or updates the config.yaml file with the contents from the CONFIG_FILE_TEMPLATE.
 * Dynamically populates fields like 'os' and 'appName', without modifying the template file.
 * @param appName the application name to be stored in the config file.
 */
private static void initConfigFile(String appName) {
    File configFile = new File(configPath);

    // Load the template configuration (using existing loadConfig)
    Map<String, String> templateConfig = loadConfig(CONFIG_FILE_TEMPLATE);

    // Add dynamic fields
    templateConfig.put("os", OS);

    // Load existing configuration if the file exists
    Map<String, String> existingConfig = new HashMap<>();
    if (configFile.exists()) {
        existingConfig = loadConfig(configPath);
    }

    // Merge existing configuration with the template (existing config values take precedence)
    Map<String, String> mergedConfig = new HashMap<>(templateConfig);  // Start with template values
    mergedConfig.putAll(existingConfig);  // Overwrite with existing config if it exists

    // Write the merged configuration back to the file
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
        for (Map.Entry<String, String> entry : mergedConfig.entrySet()) {
            writer.write(entry.getKey() + ": \"" + entry.getValue() + "\"");
            writer.newLine();
        }
        System.out.println("Config file created/updated at: " + configFile.getAbsolutePath());
    } catch (IOException e) {
        throw new RuntimeException("Failed to write config file: " + configFile.getAbsolutePath(), e);
    }
}


}
