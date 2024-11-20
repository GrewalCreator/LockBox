package com.lock.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class ConfigUtil {

    private static final String CONFIG_FILE_TEMPLATE = "/com/lock/resources/config_template.yaml";
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static String configPath;
    private String baseDir;
    private Yaml yaml;

    public ConfigUtil() {
        this.yaml = new Yaml();
        setupConfig();
    }

    /**
     * Sets up the configuration directory and config file.
     */
    private void setupConfig() {
        String userHome = System.getProperty("user.home");
        String appName = (String) loadConfigFromClassPath(CONFIG_FILE_TEMPLATE).get("appName");

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
        return (String) getConfigAttribute("appName");
    }

    public String getBaseDir(){
        return this.baseDir;
    }

    public static String getConfigPath() {
        return configPath;
    }

    /**
     * Searches the local config.yaml file for the specified key and returns its value.
     * The value can be of any type such as String, Integer, or Boolean.
     *
     * @param key the key to search for in config.yaml.
     * @return the value associated with the key, or throw exception if the key is not found.
     */
    public Object getConfigAttribute(String key) throws IllegalArgumentException {
        try (InputStream inputStream = new FileInputStream(configPath)) {
            Map<String, Object> configMap = this.yaml.load(inputStream);

            if (configMap == null) {
                throw new IllegalArgumentException("Config file is empty or could not be loaded.");
            }

            String[] keys = key.split("\\.");
            Object value = configMap;

            // Traverse the nested maps using the keys
            for (String k : keys) {
                if (value instanceof Map) {
                    Map<?, ?> mapValue = (Map<?, ?>) value;
                    // Ensure the map has the expected type (String -> Object)
                    if (mapValue.containsKey(k)) {
                        value = mapValue.get(k);
                    } else {
                        throw new IllegalArgumentException("Key '" + k + "' not found in the config path: " + key);
                    }
                } else {
                    throw new IllegalArgumentException("The key path '" + key + "' does not correspond to a valid map in the config file.");
                }
            }

            return value;
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Config file not found at: " + configPath, e);
        } catch (IOException e) {
            throw new RuntimeException("Error reading config file at: " + configPath, e);
        }
    }




    /**
     * Loads the configuration from a YAML file using SnakeYAML.
     * @param resourcePath the path to the configuration file in the classpath.
     * @return a map of configuration key-value pairs.
     */
    private Map<String, Object> loadConfigFromClassPath(String resourcePath) {
        Map<String, Object> configMap = null;

        try (InputStream inputStream = ConfigUtil.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Resource not found: " + resourcePath);
            }
            configMap = this.yaml.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return configMap;
    }

    private Map<String, Object> loadConfigFromFileSystem(String filePath) {
        Map<String, Object> configMap = null;
        File configFile = new File(filePath);

        if (!configFile.exists()) {
            System.err.println("Config file not found at: " + filePath);
            return null;
        }

        try (InputStream inputStream = new FileInputStream(configFile)) {
            configMap = this.yaml.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return configMap;
    }


    /**
     * Initializes or updates the config.yaml file with the contents from the CONFIG_FILE_TEMPLATE.
     * Dynamically populates fields like 'os', without modifying the template file.
     * @param appName the application name to be stored in the config file.
     */
    private void initConfigFile(String appName) {
        File configFile = new File(configPath);
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);  // Make the YAML output more readable
        options.setIndent(4);


        Yaml yaml = new Yaml(options);

        // Load the template configuration from classpath
        Map<String, Object> templateConfig = loadConfigFromClassPath(CONFIG_FILE_TEMPLATE);
        templateConfig.put("os", OS);  // Add the OS-specific value

        // Load existing configuration from the filesystem if it exists
        Map<String, Object> existingConfig = new HashMap<>();
        if (configFile.exists()) {
            existingConfig = loadConfigFromFileSystem(configPath);
        }

        // Merge configurations while maintaining data types
        for (Map.Entry<String, Object> entry : templateConfig.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // If the key already exists in the existing config, keep the existing value
            if (!existingConfig.containsKey(key)) {
                existingConfig.put(key, value);
            }
        }

        // Write the merged configuration back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            yaml.dump(existingConfig, writer);
            System.out.println("Config file created/updated at: " + configFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write config file: " + configFile.getAbsolutePath(), e);
        }
    }
}
