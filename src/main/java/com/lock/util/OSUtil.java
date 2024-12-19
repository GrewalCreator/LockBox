package com.lock.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.lock.util.LoggerUtil.LogLevel;

public final class OSUtil {

    private static Path baseDir;
    private static String os;

    private OSUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void init(String appName) throws UnsupportedOperationException{
        os = getOperatingSystem();
        setBaseDir(appName);
    }

    public static String getOperatingSystem() throws UnsupportedOperationException {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "windows";
        } else if (os.contains("mac")) {
            return "mac";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return "linux";
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + os);
        }
    }

    public static Path getBaseDir(){
        return baseDir;
    }


    protected static void setBaseDir(String appName) throws UnsupportedOperationException{
        String userHome = System.getProperty("user.home");


        switch (os) {
            case "windows":
                baseDir = Paths.get(System.getenv("LOCALAPPDATA"), appName);
                break;
            case "mac":
                baseDir = Paths.get(userHome, "Library", "Application Support", appName);
                break;
            case "linux":
                baseDir = Paths.get(userHome, "." + appName.toLowerCase());
                break;
            default:
                throw new UnsupportedOperationException();
        }


        if (!Files.exists(baseDir)) {
                try {
                    Files.createDirectories(baseDir);
                } catch (IOException e) {
                    System.err.println("Error Creating App Directory");
                    System.exit(1);
                }
        }

    }

}
