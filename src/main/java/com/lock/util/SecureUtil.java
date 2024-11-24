package com.lock.util;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.function.Consumer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.file.Path;


/* Reference Docs
* https://learn.microsoft.com/en-us/dotnet/api/java.security.securerandom?view=net-android-34.0
*/

// TODO: Validate Generated and User Inputted Passwords

@Retention(RetentionPolicy.RUNTIME)
@interface SecureUsageOnly {}


public final class SecureUtil {
    private static final SecureRandom random;
    private static final Path ENCRYPTED_FILE_PATH;

    private SecureUtil(){
        throw new UnsupportedOperationException("Utility class");
    }

    static {
        random = new SecureRandom();
        ENCRYPTED_FILE_PATH = OSUtil.getSecretPath();
        initEncryptedFile();
    }

    private static void initEncryptedFile(){
        ;
    }

    public static char[] generateRandomPassword(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than 0");
        }

        byte[] tempPass = new byte[length];
        random.nextBytes(tempPass);

        char[] password = new char[length];
        for (int i = 0; i < length; i++) {
            // Use the ASCII range
            password[i] = (char) (tempPass[i] & 0xFF);
        }

        return password;
    }


    /*
    * NOTE: The use of char[] is a best effort attempt to avoid Immutable Strings as much as possible but in no way garuntees protection
    */
    public static void setPassword(char[] password){
        // Set In DB
        encrypt(password);

        Arrays.fill(password, '\0');

    }

    public static void setAppPassword(char[] password){
        // Store somewhere safe outside of db
        encrypt(password);

        Arrays.fill(password, '\0');
    }

    private static byte[] getEncryptedPassword(){
        byte[] x = new byte[2];
        return x;
    }

    private static void encrypt(char[] secret){;}

    private static char[] decrypt(byte[] secret){
        char[] arr = new char[3];
        return arr;
    }

    @SecureUsageOnly
    public static void usePassword(Consumer<char[]> action) {
        byte[] encryptedPassword = getEncryptedPassword();
        if (encryptedPassword == null) {
            throw new IllegalStateException("Password is not set!");
        }

        char[] password = decrypt(encryptedPassword);
        try {
            action.accept(password); // Pass the password directly to the consumer
        } finally {
            Arrays.fill(password, '\0'); // Clear decrypted password from memory
        }
    }
}
