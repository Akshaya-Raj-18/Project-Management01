package com.pms.service;

import java.util.Optional;
import com.pms.exception.DatabaseOperationException;
import com.pms.model.User;

/**
 * Authentication and Session Management Service Interface.
 * (Module 3: Interface Segregation and Abstraction)
 */
public interface IAuthService {

    /**
     * Authenticates a user by username and password.
     *
     * @param username user login handle
     * @param password raw password
     * @return Authenticated User instance
     * @throws DatabaseOperationException if database lookup fails
     */
    Optional<User> authenticate(String username, String password) throws DatabaseOperationException;

    /**
     * Retrieves currently active session user.
     */
    User getCurrentUser();

    /**
     * Sets currently active user in session context.
     */
    void setCurrentUser(User user);

    /**
     * Clears active session.
     */
    void logout();

    /**
     * Verifies if any user is currently authenticated.
     */
    boolean isAuthenticated();
}
