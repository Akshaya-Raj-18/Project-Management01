package com.pms.dao;

import java.util.List;
import java.util.Optional;
import com.pms.exception.DatabaseOperationException;
import com.pms.model.User;
import com.pms.model.UserRole;

/**
 * Data Access Object contract for User operations.
 * (Module 3: Custom Interface Inheritance)
 */
public interface IUserDao extends Repository<User, Integer> {

    /**
     * Locate user by unique username.
     */
    Optional<User> findByUsername(String username) throws DatabaseOperationException;

    /**
     * Retrieve all active users filtered by role.
     */
    List<User> findByRole(UserRole role) throws DatabaseOperationException;

    /**
     * Authenticate user credentials.
     */
    Optional<User> authenticate(String username, String password) throws DatabaseOperationException;
}
