package com.pms.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Enterprise Application Logger.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 1: Variable-length arguments (varargs: Object... args), Functions,
 *   String formatting, Switch control statement.
 * - Module 2: Static members (static variables, static methods), Method overloading,
 *   Final variables.
 */
public final class AppLogger {

    // Module 2: Static final constants
    private static final DateTimeFormatter TIMESTAMP_FORMAT = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    // Module 2: Static variable to control system-wide logging threshold
    private static LogLevel currentLevel = LogLevel.INFO;

    // Module 2: Private constructor to prevent instantiation of utility class
    private AppLogger() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Set active logging threshold level.
     */
    public static void setLogLevel(LogLevel level) {
        if (level != null) {
            currentLevel = level;
        }
    }

    public static LogLevel getLogLevel() {
        return currentLevel;
    }

    /**
     * Core logging function with varargs (Variable-length argument list).
     * (Module 1: Varargs syntax 'Object... args', Module 1: Switch statement)
     */
    public static void log(LogLevel level, String message, Object... args) {
        if (level == null || level.ordinal() < currentLevel.ordinal()) {
            return;
        }

        final String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        final String formattedMessage;

        // Module 1: Conditional formatting handling varargs
        if (args != null && args.length > 0) {
            formattedMessage = String.format(message, args);
        } else {
            formattedMessage = message;
        }

        final String logLine = String.format("%s %s [%s] %s",
                timestamp,
                level.getPrefix(),
                Thread.currentThread().getName(),
                formattedMessage);

        // Module 1: Switch statement demonstrating control flow
        switch (level) {
            case ERROR:
                System.err.println(logLine);
                break;
            case WARN:
            case INFO:
            case DEBUG:
            default:
                System.out.println(logLine);
                break;
        }
    }

    // Module 2: Method Overloading (log with simple String vs varargs)
    public static void log(LogLevel level, String message) {
        log(level, message, (Object[]) null);
    }

    // Module 2: Overloaded convenience methods
    public static void debug(String message, Object... args) {
        log(LogLevel.DEBUG, message, args);
    }

    public static void info(String message, Object... args) {
        log(LogLevel.INFO, message, args);
    }

    public static void warn(String message, Object... args) {
        log(LogLevel.WARN, message, args);
    }

    public static void error(String message, Object... args) {
        log(LogLevel.ERROR, message, args);
    }

    public static void error(String message, Throwable throwable, Object... args) {
        String msg = (args != null && args.length > 0) ? String.format(message, args) : message;
        log(LogLevel.ERROR, "%s - Exception: %s: %s", msg, 
                throwable.getClass().getSimpleName(), throwable.getMessage());
        if (currentLevel == LogLevel.DEBUG) {
            throwable.printStackTrace(System.err);
        }
    }
}
