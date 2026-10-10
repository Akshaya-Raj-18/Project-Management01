package com.pms.util;

/**
 * LogLevel enumeration representing logging severity levels.
 * (Module 1: Enumerations and Type Safety)
 */
public enum LogLevel {
    DEBUG("[DEBUG]"),
    INFO("[INFO ]"),
    WARN("[WARN ]"),
    ERROR("[ERROR]");

    private final String prefix;

    LogLevel(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
