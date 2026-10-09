package com.pms;

import java.util.Optional;
import javax.swing.SwingUtilities;
import com.pms.dao.DatabaseConnectionManager;
import com.pms.dao.IProjectDao;
import com.pms.dao.ITaskDao;
import com.pms.dao.IUserDao;
import com.pms.dao.ProjectDaoImpl;
import com.pms.dao.TaskDaoImpl;
import com.pms.dao.UserDaoImpl;
import com.pms.gui.MainFrame;
import com.pms.gui.dialog.LoginDialog;
import com.pms.gui.theme.UITheme;
import com.pms.model.User;
import com.pms.service.AuthService;
import com.pms.service.IAuthService;
import com.pms.service.IProjectService;
import com.pms.service.ITaskService;
import com.pms.service.ProjectService;
import com.pms.service.TaskService;
import com.pms.util.AppLogger;
import com.pms.util.CommandLineParser;
import com.pms.util.DbInitializer;

/**
 * Enterprise Application Entry Point.
 * 
 * KTU PBCST304 Syllabus Topics Demonstrated:
 * - Module 1: Command Line Arguments (String[] args), Functions, Control Statements.
 * - Module 2: Anonymous Inner Classes (SwingUtilities.invokeLater Runnable).
 * - Module 3: Enterprise Architecture & Dependency Inversion (Wiring DAOs, Services, Controllers).
 * - Module 4: Event-Driven GUI bootstrap and JDBC lifecycle initialization.
 */
public class Main {

    public static void main(String[] args) {
        // Module 1: Parsing Command Line Arguments
        CommandLineParser cliConfig = CommandLineParser.parse(args);
        AppLogger.setLogLevel(cliConfig.getLogLevel());

        AppLogger.info("======================================================================");
        AppLogger.info("Starting KTU PBCST304 Project Management System (Enterprise Architecture)");
        AppLogger.info("Database File: %s", cliConfig.getDbPath());
        AppLogger.info("Seed Demo Data: %b", cliConfig.isSeedData());
        AppLogger.info("Logging Level: %s", cliConfig.getLogLevel());
        AppLogger.info("======================================================================");

        try {
            // Module 3: Singleton Pattern - configure DB path
            DatabaseConnectionManager.configure(cliConfig.getDbPath());
            DatabaseConnectionManager dbManager = DatabaseConnectionManager.getInstance();

            // Module 4: Schema initialization and demo seed generation
            DbInitializer dbInitializer = new DbInitializer(dbManager);
            dbInitializer.initializeSchema();

            if (cliConfig.isSeedData()) {
                dbInitializer.seedInitialData();
            }

            // Dependency Injection (SOLID Principles: SRP & DIP)
            IUserDao userDao = new UserDaoImpl(dbManager);
            IProjectDao projectDao = new ProjectDaoImpl(dbManager);
            ITaskDao taskDao = new TaskDaoImpl(dbManager);

            IAuthService authService = new AuthService(userDao);
            IProjectService projectService = new ProjectService(projectDao, taskDao);
            ITaskService taskService = new TaskService(taskDao);

            // Module 4: Swing UI Look & Feel Initialization
            UITheme.initializeLookAndFeel();

            // Module 2 & 4: Anonymous Inner Class on Swing Event Dispatch Thread (EDT)
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Display Authentication Dialog
                        LoginDialog loginDialog = new LoginDialog(null, authService);
                        loginDialog.setVisible(true);

                        if (loginDialog.isAuthenticated()) {
                            User currentUser = authService.getCurrentUser();
                            AppLogger.info("User authenticated: %s (%s). Launching MainFrame.",
                                    currentUser.getFullName(), currentUser.getRole());

                            MainFrame mainFrame = new MainFrame(
                                    authService, projectService, taskService, userDao, currentUser);
                            mainFrame.setVisible(true);
                        } else {
                            AppLogger.info("Authentication skipped or aborted. Exiting.");
                            System.exit(0);
                        }
                    } catch (Exception e) {
                        AppLogger.error("Fatal error during GUI initialization", e);
                    }
                }
            });

        } catch (Exception e) {
            AppLogger.error("Fatal exception encountered during application bootstrap", e);
            System.exit(1);
        }
    }
}
