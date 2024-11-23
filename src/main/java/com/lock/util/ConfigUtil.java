package com.lock.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public final class ConfigUtil {

    private static final String CONFIG_FILE_TEMPLATE = "/com/lock/resources/config_template.yaml";
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static String baseDir;
    private static String configPath;
    private static final Yaml yaml;

    // Static initialization block for one-time setup
    static {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setIndent(4);
        yaml = new Yaml(dumperOptions);

        setupConfig();
    }

    // Prevent instantiation of the utility class
    private ConfigUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Sets up the configuration directory and config file.
     */
    private static void setupConfig() {
        String userHome = System.getProperty("user.home");
        String appName = (String) loadConfigFromClassPath(CONFIG_FILE_TEMPLATE).get("appName");

        // Base directory for the app based on the OS
        if (OS.contains("win")) {
            baseDir = userHome + "\\AppData\\Local\\" + appName;
        } else if (OS.contains("mac")) {
            baseDir = userHome + "/Library/Application Support/" + appName;
        } else {
            baseDir = userHome + "/." + appName;
        }

        // Resources directory
        File resourcesDir = new File(baseDir, "resources");
        if (!resourcesDir.exists() && resourcesDir.mkdirs()) {
            System.out.println("Created directory: " + resourcesDir.getAbsolutePath());
        }


        configPath = new File(resourcesDir, "config.yaml").getAbsolutePath();
        initConfigFile(appName);
    }

    @SuppressWarnings("unchecked")
    public static void setConfigAttribute(String key, Object value, boolean force) {
        try {
            Map<String, Object> configMap = new HashMap<>();
            File configFile = new File(configPath);

            if (configFile.exists()) {
                try (InputStream inputStream = new FileInputStream(configFile)) {
                    Map<String, Object> loadedConfig = yaml.load(inputStream);
                    if (loadedConfig != null) {
                        configMap.putAll(loadedConfig);
                    }
                }
            }

            String[] keys = key.split("\\.");
            Map<String, Object> currentMap = configMap;
            for (int i = 0; i < keys.length - 1; i++) {
                String part = keys[i];
                if (!currentMap.containsKey(part) || !(currentMap.get(part) instanceof Map)) {
                    if (force) {
                        currentMap.put(part, new HashMap<>());
                    } else {
                        throw new IllegalArgumentException("Key path '" + key + "' does not exist. Set force=true to create it.");
                    }
                }
                currentMap = (Map<String, Object>) currentMap.get(part);
            }

            String finalKey = keys[keys.length - 1];
            if (!currentMap.containsKey(finalKey) && !force) {
                throw new IllegalArgumentException("Key '" + finalKey + "' does not exist. Set force=true to create it.");
            }
            currentMap.put(finalKey, value);

            // Write back to file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
                yaml.dump(configMap, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error updating the config file at: " + configPath, e);
        }
    }


    public static void setConfigAttribute(String key, Object value){
        setConfigAttribute(key, value, false);
    }

    public static String getAppName() {
        return (String) getConfigAttribute("appName");
    }

    public static String getBaseDir() {
        return baseDir;
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
    public static Object getConfigAttribute(String key) throws IllegalArgumentException {
        try (InputStream inputStream = new FileInputStream(configPath)) {
            Map<String, Object> configMap = yaml.load(inputStream);
            System.out.println("Raw YAML data: " + configMap);

            if (configMap == null) {
                throw new IllegalArgumentException("Config file is empty or could not be loaded.");
            }

            String[] keys = key.split("\\.");
            Object value = configMap;

            // Traverse the nested maps using the keys
            for (int i = 0; i < keys.length; i++) {
                String k = keys[i];

                // If value is map, continue traversing it
                if (value instanceof Map) {
                    Map<?, ?> mapValue = (Map<?, ?>) value;
                    if (mapValue.containsKey(k)) {
                        value = mapValue.get(k);
                    } else {
                        throw new IllegalArgumentException("Key '" + k + "' not found in the config path: " + key);
                    }
                } else {
                    if (i == keys.length - 1) {
                        return value; // Return the final non-map value
                    } else {
                        throw new IllegalArgumentException("The key path '" + key + "' does not correspond to a valid map in the config file.");
                    }
                }
            }

            return value; // This will be reached after all keys have been traversed
        } catch (IOException e) {
            throw new RuntimeException("Error reading config file at: " + configPath, e);
        }
    }


    /**
     * Loads the configuration from a YAML file using SnakeYAML.
     * @param resourcePath the path to the configuration file in the classpath.
     * @return a map of configuration key-value pairs.
     */
    private static Map<String, Object> loadConfigFromClassPath(String resourcePath) {
        Map<String, Object> configMap = null;

        try (InputStream inputStream = ConfigUtil.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            configMap = yaml.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config from classpath: " + resourcePath, e);
        }

        return configMap;
    }

    /**
     * Loads the configuration from the local filesystem using SnakeYAML.
     * @param filePath the path to the configuration file on the filesystem.
     * @return a map of configuration key-value pairs.
     */
    private static Map<String, Object> loadConfigFromFileSystem(String filePath) {
        Map<String, Object> configMap = null;

        try (InputStream inputStream = new FileInputStream(filePath)) {
            configMap = yaml.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config from file system: " + filePath, e);
        }

        return configMap;
    }

    /**
     * Initializes or updates the config.yaml file with the contents from the CONFIG_FILE_TEMPLATE.
     * Dynamically populates fields like 'os', without modifying the template file.
     * @param appName the application name to be stored in the config file.
     */
    private static void initConfigFile(String appName) {
        File configFile = new File(configPath);

        // Load the template configuration from classpath
        Map<String, Object> templateConfig = loadConfigFromClassPath(CONFIG_FILE_TEMPLATE);

        // Load existing configuration from the filesystem if it exists
        Map<String, Object> existingConfig = new HashMap<>();
        if (configFile.exists()) {
            existingConfig = loadConfigFromFileSystem(configPath);
        }

        // Merge configurations while maintaining data types and still checking nested values
        existingConfig = deepMerge(templateConfig, existingConfig);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            yaml.dump(existingConfig, writer);
            System.out.println("Config file created/updated at: " + configFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write config file: " + configPath, e);
        }

        // Set device attributes
        setConfigAttribute("device.os", OS);
    }

    // Deep merge logic to handle nested maps
    @SuppressWarnings("unchecked")
    private static Map<String, Object> deepMerge(Map<String, Object> templateConfig, Map<String, Object> existingConfig) {
        for (Map.Entry<String, Object> entry : templateConfig.entrySet()) {
            String key = entry.getKey();
            Object templateValue = entry.getValue();

            // If the key doesn't exist in existingConfig, add it
            if (!existingConfig.containsKey(key)) {
                existingConfig.put(key, templateValue);
            } else {
                // If both values are maps, merge them recursively
                Object existingValue = existingConfig.get(key);
                if (templateValue instanceof Map && existingValue instanceof Map) {
                    existingConfig.put(key, deepMerge((Map<String, Object>) templateValue, (Map<String, Object>) existingValue));
                } else {
                    existingConfig.put(key, templateValue);
                }
            }
        }
        return existingConfig;
    }
}
