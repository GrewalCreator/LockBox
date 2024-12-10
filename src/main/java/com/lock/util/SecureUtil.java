package com.lock.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.function.Consumer;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

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
    // TODO: Think of the storage logic for keys, passwords, app passwords

public final class SecureUtil {
    private static final SecureRandom random;
    private static final Path ENCRYPTED_FILE_PATH;
    private static Cipher cipher;
    private static final Path KEYSTORE_PATH;
    private static SecretKey secretKey;

    private SecureUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    static {
        try {
            random = new SecureRandom();
            ENCRYPTED_FILE_PATH = OSUtil.getSecretPath();
            KEYSTORE_PATH = OSUtil.getKeyStorePath();

            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            secretKey = keyGenerator.generateKey();

            cipher = Cipher.getInstance("AES");
            initEncryptedFile();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing SecureUtil", e);
        }
    }

    private static void initEncryptedFile() {
        try {
            if (!Files.exists(ENCRYPTED_FILE_PATH)) {
                Files.createDirectories(ENCRYPTED_FILE_PATH.getParent());
                Files.createFile(ENCRYPTED_FILE_PATH);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing encrypted file", e);
        }
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

    public static void setPassword(char[] password) {
        try {
            byte[] encryptedPassword = encrypt(password);
            Files.write(ENCRYPTED_FILE_PATH, encryptedPassword, StandardOpenOption.WRITE);
        } catch (Exception e) {
            throw new RuntimeException("Error setting password", e);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    public static boolean setAppPassword(char[] password) throws RuntimeException{
        if(!validatePassword(password)){return false;}

        try (FileOutputStream stream = new FileOutputStream(ENCRYPTED_FILE_PATH.toFile())) {
            byte[] hashPassword = hash(password);

            stream.write(hashPassword);
        } catch (Exception e) {
            throw new RuntimeException("Error setting app password", e);
        } finally {
            Arrays.fill(password, '\0');
        }

        return true;
    }

    private static boolean validatePassword(char[] password){

        return true;
    }

    public static byte[] hash(char[] password) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, toBytes(password), 65536, 256);
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

    private static byte[] getEncryptedPassword() {
        try (FileInputStream stream = new FileInputStream(ENCRYPTED_FILE_PATH.toFile())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Error reading encrypted password", e);
        }
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

    @SecureUsageOnly
    public static void usePassword(Consumer<char[]> action) {
        byte[] encryptedPassword = getEncryptedPassword();
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
