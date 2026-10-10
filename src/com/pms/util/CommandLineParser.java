package com.pms.util;

/**
 * Command-Line Arguments Parser.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 1: Command Line Arguments handling (String[] args), Control Statements
 *   (for loop, switch/case, if-else chains), Arrays, Wrapper types (Integer, Boolean),
 *   Primitive types and casting.
 * - Module 2: Object encapsulation, getters, immutable final fields, constructor overloading.
 */
public class CommandLineParser {

    private final String dbPath;
    private final boolean seedData;
    private final String theme;
    private final LogLevel logLevel;
    private final int serverPort;

    // Default configuration constants (Module 2: static final)
    public static final String DEFAULT_DB_PATH = "pms_database.db";
    public static final boolean DEFAULT_SEED_DATA = true;
    public static final String DEFAULT_THEME = "system";
    public static final LogLevel DEFAULT_LOG_LEVEL = LogLevel.INFO;
    public static final int DEFAULT_PORT = 8080;

    // Module 2: Overloaded Constructors
    public CommandLineParser() {
        this(DEFAULT_DB_PATH, DEFAULT_SEED_DATA, DEFAULT_THEME, DEFAULT_LOG_LEVEL, DEFAULT_PORT);
    }

    public CommandLineParser(String dbPath, boolean seedData, String theme, LogLevel logLevel, int serverPort) {
        // Module 2: dynamic use of 'this' to disambiguate and assign instance variables
        this.dbPath = dbPath;
        this.seedData = seedData;
        this.theme = theme;
        this.logLevel = logLevel;
        this.serverPort = serverPort;
    }

    /**
     * Parses the command line arguments array.
     * Demonstrates Module 1: Command Line Arguments, Arrays, loops, switch statements,
     * Wrapper type parsing, and primitive casting.
     *
     * @param args Array of command-line strings passed to main()
     * @return Configured CommandLineParser instance
     */
    public static CommandLineParser parse(String[] args) {
        String parsedDbPath = DEFAULT_DB_PATH;
        boolean parsedSeed = DEFAULT_SEED_DATA;
        String parsedTheme = DEFAULT_THEME;
        LogLevel parsedLogLevel = DEFAULT_LOG_LEVEL;
        int parsedPort = DEFAULT_PORT;

        if (args != null && args.length > 0) {
            // Module 1: Standard 'for' loop processing array elements
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];

                if (arg == null || arg.trim().isEmpty()) {
                    continue;
                }

                // Module 1: Switch statement on String
                switch (arg.toLowerCase()) {
                    case "--db":
                    case "--db-path":
                        if (i + 1 < args.length) {
                            parsedDbPath = args[++i];
                        }
                        break;

                    case "--no-seed":
                        parsedSeed = false;
                        break;

                    case "--seed":
                        parsedSeed = true;
                        break;

                    case "--theme":
                        if (i + 1 < args.length) {
                            parsedTheme = args[++i];
                        }
                        break;

                    case "--log-level":
                        if (i + 1 < args.length) {
                            String levelStr = args[++i].toUpperCase();
                            try {
                                parsedLogLevel = LogLevel.valueOf(levelStr);
                            } catch (IllegalArgumentException e) {
                                AppLogger.warn("Invalid log level '%s', falling back to %s", levelStr, DEFAULT_LOG_LEVEL);
                            }
                        }
                        break;

                    case "--port":
                        if (i + 1 < args.length) {
                            try {
                                // Module 1: Wrapper class Integer parsing
                                Integer portWrapper = Integer.valueOf(args[++i]);
                                // Module 1: Primitive unboxing / casting
                                parsedPort = portWrapper.intValue();
                            } catch (NumberFormatException e) {
                                AppLogger.warn("Invalid port number '%s', using default port %d", args[i], DEFAULT_PORT);
                            }
                        }
                        break;

                    case "--help":
                    case "-h":
                        printHelp();
                        break;

                    default:
                        AppLogger.warn("Unrecognized command line argument: %s", arg);
                        break;
                }
            }
        }

        return new CommandLineParser(parsedDbPath, parsedSeed, parsedTheme, parsedLogLevel, parsedPort);
    }

    private static void printHelp() {
        System.out.println("===============================================================");
        System.out.println("  KTU PBCST304 - Project Management System (PMS) CLI Options   ");
        System.out.println("===============================================================");
        System.out.println("  --db-path <path>    Specify custom SQLite database file path");
        System.out.println("  --seed              Populate demo seed data if DB is empty (default: true)");
        System.out.println("  --no-seed           Skip seeding demo data");
        System.out.println("  --theme <name>      UI Theme: 'system', 'light', 'dark'");
        System.out.println("  --log-level <level> Logging level: DEBUG, INFO, WARN, ERROR");
        System.out.println("  --port <number>     Server or monitoring port");
        System.out.println("  --help, -h          Display this help message");
        System.out.println("===============================================================");
    }

    public String getDbPath() {
        return dbPath;
    }

    public boolean isSeedData() {
        return seedData;
    }

    public String getTheme() {
        return theme;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public int getServerPort() {
        return serverPort;
    }
}
