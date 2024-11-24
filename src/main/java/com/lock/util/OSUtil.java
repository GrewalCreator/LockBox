package com.lock.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class OSUtil {

    private static Path baseDir;
    private static String os;

    private OSUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    static {
        setBaseDir();
        os = getOperatingSystem();
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


    private static void setBaseDir(){
        String appName = ConfigUtil.getAppName();
        String userHome = System.getProperty("user.home");


        switch (os) {
            case "windows":
                baseDir = Paths.get(System.getenv("LOCALAPPDATA"), appName);
            case "mac":
                baseDir = Paths.get(userHome, "Library", "Application Support", appName);
            case "linux":
                baseDir = Paths.get(userHome, "." + appName.toLowerCase());
            default:
                throw new UnsupportedOperationException("Unsupported OS: " + os);
        }
    }

    public static Path getSecretPath(){
        return getBaseDir().resolve(".crypt.enc");
    }

}
