package com.lock.util;

import java.security.SecureRandom;
import java.util.Base64;

/* Reference Docs
* https://learn.microsoft.com/en-us/dotnet/api/java.security.securerandom?view=net-android-34.0
*/

public class SecureUtil {
    private static final SecureRandom random;

    private SecureUtil(){
        throw new UnsupportedOperationException("Utility class");
    }

    static {
        random = new SecureRandom();
    }

    public static String generateRandomPassword(int length){
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than 0");
        }
        
        byte seed[] = random.generateSeed(20);
        random.setSeed(seed);

        byte[] tempPass = new byte[length];
        random.nextBytes(tempPass);

        return Base64.getEncoder().encodeToString(tempPass).substring(0, length);

    }
}
