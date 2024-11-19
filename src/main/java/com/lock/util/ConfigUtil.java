package com.lock.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigUtil {

    private static final String CONFIG_FILE = "/com/lock/resources/config.yaml";

    public static String getAppName() {
        Map<String, String> configMap = loadConfig();
        return configMap.get("appName");
    }

    private static Map<String, String> loadConfig() {
        Map<String, String> configMap = new HashMap<>();
        try (InputStream inputStream = ConfigUtil.class.getResourceAsStream(CONFIG_FILE)){
            if (inputStream == null) {
                throw new RuntimeException(CONFIG_FILE + " not found in classpath");
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
}
