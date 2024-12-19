package com.lock.util;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.function.Consumer;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.lock.util.LoggerUtil.LogLevel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;

@Retention(RetentionPolicy.RUNTIME)
@interface SecureUsageOnly {}

    /*
    * NOTE: The use of char[] is a best effort attempt to avoid Immutable Strings as much as possible but in no way garuntees protection
    */
    

    /*
     * Password Storage:
     * Key Part 1 User password hash stored in encrypted file
     * Key Part 2 stored in local keystorage
     * Key Part 3 Stored remotely, temporarily in local
     * xOr all key parts for key
     */

public final class SecureUtil {
    private static final SecureRandom random;
    private static final Path ENCRYPTED_FILE_PATH;
    private static Cipher cipher;
    private static final Path KEYSTORE_PATH;
    private static SecretKey secretKey;
    private static byte[] encryptedPassword;
    private static final String PASSWORD_SALT_FILE = ".password_salt.bin";
    private static final String KEY_SALT_FILE = ".key_salt.bin";

    private static byte[] TEMP_REMOTE_SHARE;

    private SecureUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    static {
        random = new SecureRandom();
        ENCRYPTED_FILE_PATH = OSUtil.getBaseDir().resolve("resources/.crypt.enc");
        KEYSTORE_PATH = OSUtil.getBaseDir().resolve("resources/.keystore.jks");
        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            LoggerUtil.writeLog(LogLevel.ERROR, "Cipher initialization failed: " + e.getMessage());
        }
    }

    public static void initSecurity(){
        initEncryptedFile();
        initKeyStoreFile();
    }

    private static void initEncryptedFile(){
        try {
            if (!Files.exists(ENCRYPTED_FILE_PATH)) {
                Files.createDirectories(ENCRYPTED_FILE_PATH.getParent());
                Files.createFile(ENCRYPTED_FILE_PATH);
            }
        } catch (Exception e) {
            LoggerUtil.writeLog(LogLevel.ERROR, "Encrypted File initialization failed: " + e.getMessage());
        }
    }

    private static void initKeyStoreFile(){
        try {
            if (!Files.exists(KEYSTORE_PATH)) {
                Files.createDirectories(KEYSTORE_PATH.getParent());
                Files.createFile(KEYSTORE_PATH);
            }
        } catch (Exception e) {
            LoggerUtil.writeLog(LogLevel.ERROR, "KeyStore File initialization failed: " + e.getMessage());
        }
    }

    public static boolean setNewAppPassword(char[] password) throws RuntimeException{
        if(!validatePassword(password)){return false;}

        try (FileOutputStream stream = new FileOutputStream(ENCRYPTED_FILE_PATH.toFile())) {
            byte[] salt = generateSalt();
            byte[] hashPassword = hash(password, salt);
            storeSalt(PASSWORD_SALT_FILE, salt);
            stream.write(hashPassword);

            generateKey(password);

            encryptedPassword = encrypt(password);
        } catch (Exception e) {
            throw new RuntimeException("Error setting app password", e);
        } finally {
            Arrays.fill(password, '\0');
        }

        return true;
    }

    public static void setAppPassword(char[] password){
        try {
            encryptedPassword = encrypt(password);
            buildKey(password);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally{
            Arrays.fill(password, '\0');
        }
    }

    private static void setLocalShare(byte[] localShare){
        try (FileOutputStream stream = new FileOutputStream(KEYSTORE_PATH.toFile())) {
            stream.write(localShare);

        } catch (Exception e) {
            throw new RuntimeException("Error setting app password", e);
        } finally {
            Arrays.fill(localShare, (byte) 0);
        }
    }

    private static void setRemoteShare(byte[] remoteShare){
        // Temporary implementation
        TEMP_REMOTE_SHARE = remoteShare;
    }


    private static void generateKey(char[] password){
        byte[] salt = generateSalt();
        storeSalt(KEY_SALT_FILE, salt);

        try {
            // Derive the password part of the key
            byte[] passwordPart = derivePasswordKey(password, salt, 10000, 32);

            // Generate the random share
            byte[] randomShare = new byte[passwordPart.length];
            random.nextBytes(randomShare);

            // Calculate the remote share (passwordPart XOR randomShare)
            byte[] remoteShare = new byte[passwordPart.length];
            for (int i = 0; i < passwordPart.length; i++) {
                remoteShare[i] = (byte) (passwordPart[i] ^ randomShare[i]);
            }

            // XOR all shares to reconstruct the final key
            byte[] finalKey = new byte[passwordPart.length];
            for (int i = 0; i < passwordPart.length; i++) {
                finalKey[i] = (byte) (passwordPart[i] ^ randomShare[i] ^ remoteShare[i]);
            }

            // Initialize the SecretKey
            SecretKey newKey = new SecretKeySpec(finalKey, "AES");

            reEncyrptPasswords(secretKey, newKey);


            setLocalShare(randomShare);
            setRemoteShare(remoteShare);



            // Clean up sensitive data
            Arrays.fill(passwordPart, (byte) 0);
            Arrays.fill(randomShare, (byte) 0);
            Arrays.fill(remoteShare, (byte) 0);
            Arrays.fill(finalKey, (byte) 0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void buildKey(char[] password) {
        try {
            byte[] localShare;
            try (FileInputStream stream = new FileInputStream(KEYSTORE_PATH.toFile())) {
                localShare = stream.readAllBytes();
            }


            byte[] salt = retrieveSalt(KEY_SALT_FILE);
            byte[] passwordPart = derivePasswordKey(password, salt, 10000, 32);

            // Step 3: Retrieve the remote share (temporarily stored in memory for this implementation)
            if (TEMP_REMOTE_SHARE == null) {
                throw new IllegalStateException("Remote share is not available");
            }
            byte[] remoteShare = TEMP_REMOTE_SHARE;

            // Step 4: XOR the three parts (passwordPart, localShare, remoteShare) to reconstruct the final key
            if (passwordPart.length != localShare.length || localShare.length != remoteShare.length) {
                throw new IllegalStateException("Key parts have mismatched lengths");
            }

            byte[] finalKey = new byte[passwordPart.length];
            for (int i = 0; i < finalKey.length; i++) {
                finalKey[i] = (byte) (passwordPart[i] ^ localShare[i] ^ remoteShare[i]);
            }

            // Step 5: Initialize the secretKey field with the reconstructed key
            secretKey = new SecretKeySpec(finalKey, "AES");

            // Step 6: Clean up sensitive data
            Arrays.fill(passwordPart, (byte) 0);
            Arrays.fill(localShare, (byte) 0);
            Arrays.fill(remoteShare, (byte) 0);
            Arrays.fill(finalKey, (byte) 0);
        } catch (Exception e) {
            throw new RuntimeException("Error rebuilding the key", e);
        } finally {
            // Ensure the password is cleared from memory
            Arrays.fill(password, '\0');
        }
    }


    private static byte[] derivePasswordKey(char[] password, byte[] salt, int iterations, int length) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, length * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return skf.generateSecret(spec).getEncoded();
    }


    private static void reEncyrptPasswords(SecretKey oldKey, SecretKey newKey) {
        //TODO: Implement password rencryption
        secretKey = newKey;
    }

    public static char[] generateRandomPassword(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than 0");
        }

        byte[] tempPass = new byte[length];
        random.nextBytes(tempPass);

        char[] password = new char[length];
        for (int i = 0; i < length; i++) {
            password[i] = (char) ((tempPass[i] & 0x7F) % 94 + 33);
        }

        return password;
    }

    private static boolean validatePassword(char[] password) {
        if (password.length < 8) {
            return false;
        }

        boolean hasLower = false;
        boolean hasUpper = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (char c : password) {
            if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
            }
        }

        return hasLower && hasUpper && hasDigit && hasSpecialChar;
    }



    public static boolean verifyLogin(char[] password) throws Exception {
        // Read Encrypted File
        byte[] storedHash;
        try (FileInputStream stream = new FileInputStream(ENCRYPTED_FILE_PATH.toFile())) {
            storedHash = stream.readAllBytes();
        }

        byte[] salt = retrieveSalt(PASSWORD_SALT_FILE);

        byte[] enteredPasswordHash = hash(password, salt);

        return Arrays.equals(enteredPasswordHash, storedHash);
    }


    public static byte[] hash(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, 100_000, 512);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hashedPassword = factory.generateSecret(spec).getEncoded();

        Arrays.fill(password, '\0');
        return hashedPassword;
    }

    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    public static byte[] encrypt(char[] plainText) throws Exception {
        byte[] plainTextBytes = toBytes(plainText);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(plainTextBytes);
    }

    private static char[] decrypt(byte[] encryptedBytes) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return toChars(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting password", e);
        }
    }

    private static void storeSalt(String saltName, byte[] salt) {
        Path saltPath = OSUtil.getBaseDir().resolve(saltName);

        // Ensure the directory exists
        try {
            Files.createDirectories(saltPath.getParent());

            if (Files.notExists(saltPath)) {
                Files.createFile(saltPath);
            }

            try (FileOutputStream stream = new FileOutputStream(saltPath.toFile())) {
                stream.write(salt);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error storing salt: " + saltName, e);
        }
    }

    private static byte[] retrieveSalt(String saltName) {
        Path saltPath = OSUtil.getBaseDir().resolve(saltName);
        try (FileInputStream stream = new FileInputStream(saltPath.toFile())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving salt: " + saltName, e);
        }
    }


    @SecureUsageOnly
    public static void usePassword(Consumer<char[]> action) {
        if (encryptedPassword == null) {
            throw new IllegalStateException("Password is not set");
        }

        char[] password = decrypt(encryptedPassword);
        try {
            action.accept(password);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private static byte[] toBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0);
        return bytes;
    }

    private static char[] toChars(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        CharBuffer charBuffer = Charset.forName("UTF-8").decode(byteBuffer);
        char[] chars = Arrays.copyOfRange(charBuffer.array(), charBuffer.position(), charBuffer.limit());
        Arrays.fill(charBuffer.array(), '\0');
        return chars;
    }
}
