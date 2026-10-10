package com.pms.service;

import java.util.Optional;
import com.pms.dao.IUserDao;
import com.pms.exception.DatabaseOperationException;
import com.pms.model.User;
import com.pms.util.AppLogger;

/**
 * Authentication Service Implementation.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 2: Object references, Encapsulation.
 * - Module 3: SOLID Principles (Single Responsibility Principle, Dependency Inversion Principle).
 */
public class AuthService implements IAuthService {

    // Module 3: Dependency Inversion - depends on abstraction IUserDao
    private final IUserDao userDao;
    private User currentUser;

    public AuthService(IUserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Optional<User> authenticate(String username, String password) throws DatabaseOperationException {
        if (username == null || username.trim().isEmpty() || password == null) {
            return Optional.empty();
        }

        Optional<User> userOpt = userDao.authenticate(username.trim(), password);
        if (userOpt.isPresent()) {
            this.currentUser = userOpt.get();
            AppLogger.info("User '%s' authenticated successfully as %s", 
                    currentUser.getUsername(), currentUser.getRole());
        } else {
            AppLogger.warn("Failed authentication attempt for username: '%s'", username);
        }
        return userOpt;
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @Override
    public void logout() {
        if (currentUser != null) {
            AppLogger.info("User '%s' logged out", currentUser.getUsername());
        }
        this.currentUser = null;
    }

    @Override
    public boolean isAuthenticated() {
        return currentUser != null;
    }
}
