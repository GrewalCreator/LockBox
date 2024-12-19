package com.lock.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public final class LoggerUtil {
    private static final Path baseDir = OSUtil.getBaseDir();
    private static final AtomicInteger reportId = new AtomicInteger(0);
    private static final ReentrantLock fileLock = new ReentrantLock();
    private static final Path logPath = baseDir.resolve("resources/logs/");

    public enum LogLevel {
        INFO, WARN, ERROR, DEBUG
    }

    private LoggerUtil() {
        throw new UnsupportedOperationException("Logger class");
    }

    public static void initLogger() {
        if (!Files.exists(logPath)) {
            try {
                Files.createDirectories(logPath);
            } catch (IOException e) {
                System.err.println("Logging failed: " + e.getMessage());
                e.printStackTrace();
            }
            writeLog(LogLevel.INFO, "Created directory: " + logPath.toString());
        }

        writeLog("Starting LockBox . . .");
    }

    public static void writeLog(LogLevel level, String message) {
        try {
            fileLock.lock();
            int currentReportId = reportId.incrementAndGet();

            String fileName = generateLogFileName(currentReportId);
            Path logFile = logPath.resolve(fileName);

            if (!Files.exists(logFile)) {
                Files.createFile(logFile);
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile.toFile(), true))) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                writer.write(String.format("[%s] [%s] %s%n", timestamp, level, message));
                writer.flush();
            }
        } catch (IOException e) {
            // Fallback logging mechanism
            System.err.println("Logging failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            fileLock.unlock();
            if (level == LogLevel.ERROR){ System.exit(1); }
        }
    }

    public static void writeLog(String message) {
        writeLog(LogLevel.INFO, message);
    }

    private static String generateLogFileName(int reportId) {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("%s_%d.log", currentDate.format(formatter), reportId);
    }

    public int getCurrentReportId() {
        return reportId.get();
    }
}