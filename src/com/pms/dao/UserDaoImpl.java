package com.pms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.pms.exception.DatabaseOperationException;
import com.pms.model.Developer;
import com.pms.model.Manager;
import com.pms.model.User;
import com.pms.model.UserRole;
import com.pms.util.AppLogger;

/**
 * JDBC Implementation of IUserDao.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 2: Polymorphism (instantiating Manager or Developer based on DB record),
 *   Dynamic Method Dispatch.
 * - Module 3: try-catch-finally resource management, Interface implementation.
 * - Module 4: JDBC CRUD operations (PreparedStatement, ResultSet, SQL query execution).
 */
public class UserDaoImpl implements IUserDao {

    private final DatabaseConnectionManager connectionManager;

    public UserDaoImpl() {
        this(DatabaseConnectionManager.getInstance());
    }

    public UserDaoImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public User save(User user) throws DatabaseOperationException {
        final String sql = "INSERT INTO users (username, password_hash, full_name, email, role, " +
                "department, tech_stack, hourly_rate, max_hours, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getPasswordHash());
                stmt.setString(3, user.getFullName());
                stmt.setString(4, user.getEmail());
                stmt.setString(5, user.getRole().name());

                // Polymorphic attribute mapping
                if (user instanceof Manager) {
                    Manager mgr = (Manager) user;
                    stmt.setString(6, mgr.getDepartment());
                    stmt.setNull(7, java.sql.Types.VARCHAR);
                    stmt.setDouble(8, mgr.getBudgetApprovalLimit());
                    stmt.setNull(9, java.sql.Types.INTEGER);
                } else if (user instanceof Developer) {
                    Developer dev = (Developer) user;
                    stmt.setNull(6, java.sql.Types.VARCHAR);
                    stmt.setString(7, dev.getPrimaryTechStack());
                    stmt.setDouble(8, dev.getHourlyBillingRate());
                    stmt.setInt(9, dev.getMaxWeeklyHours());
                } else {
                    stmt.setNull(6, java.sql.Types.VARCHAR);
                    stmt.setNull(7, java.sql.Types.VARCHAR);
                    stmt.setDouble(8, 0.0);
                    stmt.setNull(9, java.sql.Types.INTEGER);
                }

                stmt.setInt(10, user.isActive() ? 1 : 0);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            user.setId(generatedKeys.getInt(1));
                        }
                    }
                }
                return user;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save user: " + user.getUsername(), sql, e);
        }
    }

    @Override
    public Optional<User> findById(Integer id) throws DatabaseOperationException {
        final String sql = "SELECT * FROM users WHERE id = ?";
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error finding user by id: " + id, sql, e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) throws DatabaseOperationException {
        final String sql = "SELECT * FROM users WHERE username = ? AND is_active = 1";
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error finding user by username: " + username, sql, e);
        }
    }

    @Override
    public Optional<User> authenticate(String username, String password) throws DatabaseOperationException {
        final String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ? AND is_active = 1";
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password); // In production, password hash check
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Authentication error for user: " + username, sql, e);
        }
    }

    @Override
    public List<User> findAll() throws DatabaseOperationException {
        final String sql = "SELECT * FROM users WHERE is_active = 1 ORDER BY full_name ASC";
        List<User> list = new ArrayList<>();
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching all users", sql, e);
        }
    }

    @Override
    public List<User> findByRole(UserRole role) throws DatabaseOperationException {
        final String sql = "SELECT * FROM users WHERE role = ? AND is_active = 1 ORDER BY full_name ASC";
        List<User> list = new ArrayList<>();
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, role.name());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToUser(rs));
                    }
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error finding users by role: " + role, sql, e);
        }
    }

    @Override
    public List<User> search(String keyword) throws DatabaseOperationException {
        final String sql = "SELECT * FROM users WHERE is_active = 1 AND (username LIKE ? OR full_name LIKE ? OR email LIKE ?)";
        List<User> list = new ArrayList<>();
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                String pattern = "%" + keyword + "%";
                stmt.setString(1, pattern);
                stmt.setString(2, pattern);
                stmt.setString(3, pattern);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToUser(rs));
                    }
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error searching users with keyword: " + keyword, sql, e);
        }
    }

    @Override
    public void update(User user) throws DatabaseOperationException {
        final String sql = "UPDATE users SET full_name = ?, email = ?, department = ?, tech_stack = ?, " +
                "hourly_rate = ?, max_hours = ?, is_active = ? WHERE id = ?";
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, user.getFullName());
                stmt.setString(2, user.getEmail());

                if (user instanceof Manager) {
                    Manager mgr = (Manager) user;
                    stmt.setString(3, mgr.getDepartment());
                    stmt.setNull(4, java.sql.Types.VARCHAR);
                    stmt.setDouble(5, mgr.getBudgetApprovalLimit());
                    stmt.setNull(6, java.sql.Types.INTEGER);
                } else if (user instanceof Developer) {
                    Developer dev = (Developer) user;
                    stmt.setNull(3, java.sql.Types.VARCHAR);
                    stmt.setString(4, dev.getPrimaryTechStack());
                    stmt.setDouble(5, dev.getHourlyBillingRate());
                    stmt.setInt(6, dev.getMaxWeeklyHours());
                } else {
                    stmt.setNull(3, java.sql.Types.VARCHAR);
                    stmt.setNull(4, java.sql.Types.VARCHAR);
                    stmt.setDouble(5, 0.0);
                    stmt.setNull(6, java.sql.Types.INTEGER);
                }

                stmt.setInt(7, user.isActive() ? 1 : 0);
                stmt.setInt(8, user.getId());

                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating user ID: " + user.getId(), sql, e);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DatabaseOperationException {
        // Soft delete
        final String sql = "UPDATE users SET is_active = 0 WHERE id = ?";
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting user ID: " + id, sql, e);
        }
    }

    @Override
    public long count() throws DatabaseOperationException {
        final String sql = "SELECT COUNT(*) FROM users WHERE is_active = 1";
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0L;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error counting active users", sql, e);
        }
    }

    /**
     * Polymorphic Factory Method mapping database rows to concrete User subclasses.
     * Demonstrates dynamic method dispatch and runtime polymorphism.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        String fullName = rs.getString("full_name");
        String email = rs.getString("email");
        String roleStr = rs.getString("role");

        UserRole role;
        try {
            role = UserRole.valueOf(roleStr);
        } catch (Exception e) {
            role = UserRole.DEVELOPER;
        }

        // Polymorphic instantiations
        if (role == UserRole.MANAGER) {
            String department = rs.getString("department");
            double approvalLimit = rs.getDouble("hourly_rate");
            Manager manager = new Manager(id, username, passwordHash, fullName, email,
                    department != null ? department : "Engineering", approvalLimit);
            manager.setActive(rs.getInt("is_active") == 1);
            return manager;
        } else {
            String techStack = rs.getString("tech_stack");
            double hourlyRate = rs.getDouble("hourly_rate");
            int maxHours = rs.getInt("max_hours");
            Developer developer = new Developer(id, username, passwordHash, fullName, email,
                    techStack != null ? techStack : "Full-Stack Java",
                    hourlyRate > 0 ? hourlyRate : 50.0,
                    maxHours > 0 ? maxHours : 40);
            developer.setActive(rs.getInt("is_active") == 1);
            return developer;
        }
    }
}
