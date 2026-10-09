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
import com.pms.model.Project;
import com.pms.model.ProjectStatus;

/**
 * JDBC Implementation of IProjectDao.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 1: Arrays (tags), Strings, Control statements.
 * - Module 3: Exception handling, Custom exceptions, Interface implementation.
 * - Module 4: Dynamic JDBC operations with PreparedStatement, ResultSet, and SQL statements.
 */
public class ProjectDaoImpl implements IProjectDao {

    private final DatabaseConnectionManager connectionManager;

    public ProjectDaoImpl() {
        this(DatabaseConnectionManager.getInstance());
    }

    public ProjectDaoImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Project save(Project project) throws DatabaseOperationException {
        final String sql = "INSERT INTO projects (title, description, budget, status, manager_id, " +
                "client_name, tags, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, project.getTitle());
                stmt.setString(2, project.getDescription());
                stmt.setDouble(3, project.getBudget());
                stmt.setString(4, project.getStatus().name());

                if (project.getManagerId() != null) {
                    stmt.setInt(5, project.getManagerId());
                } else {
                    stmt.setNull(5, java.sql.Types.INTEGER);
                }

                stmt.setString(6, project.getClientName());
                stmt.setString(7, project.getTagsFormatted());
                stmt.setInt(8, project.isActive() ? 1 : 0);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            project.setId(keys.getInt(1));
                        }
                    }
                }
                return project;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to persist project: " + project.getTitle(), sql, e);
        }
    }

    @Override
    public Optional<Project> findById(Integer id) throws DatabaseOperationException {
        final String sql = "SELECT * FROM projects WHERE id = ? AND is_active = 1";
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToProject(rs));
                    }
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to locate project by id: " + id, sql, e);
        }
    }

    @Override
    public List<Project> findAll() throws DatabaseOperationException {
        final String sql = "SELECT * FROM projects WHERE is_active = 1 ORDER BY id DESC";
        List<Project> list = new ArrayList<>();
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProject(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch all projects", sql, e);
        }
    }

    @Override
    public List<Project> findByManagerId(int managerId) throws DatabaseOperationException {
        final String sql = "SELECT * FROM projects WHERE manager_id = ? AND is_active = 1 ORDER BY id DESC";
        List<Project> list = new ArrayList<>();
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, managerId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToProject(rs));
                    }
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch projects by manager id: " + managerId, sql, e);
        }
    }

    @Override
    public List<Project> findByStatus(ProjectStatus status) throws DatabaseOperationException {
        final String sql = "SELECT * FROM projects WHERE status = ? AND is_active = 1 ORDER BY id DESC";
        List<Project> list = new ArrayList<>();
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status.name());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToProject(rs));
                    }
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch projects by status: " + status, sql, e);
        }
    }

    @Override
    public List<Project> findByTag(String tag) throws DatabaseOperationException {
        final String sql = "SELECT * FROM projects WHERE is_active = 1 AND tags LIKE ? ORDER BY id DESC";
        List<Project> list = new ArrayList<>();
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + tag + "%");
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToProject(rs));
                    }
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to filter projects by tag: " + tag, sql, e);
        }
    }

    @Override
    public List<Project> search(String keyword) throws DatabaseOperationException {
        final String sql = "SELECT * FROM projects WHERE is_active = 1 AND " +
                "(title LIKE ? OR description LIKE ? OR client_name LIKE ? OR tags LIKE ?)";
        List<Project> list = new ArrayList<>();
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                String pattern = "%" + keyword + "%";
                stmt.setString(1, pattern);
                stmt.setString(2, pattern);
                stmt.setString(3, pattern);
                stmt.setString(4, pattern);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToProject(rs));
                    }
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to search projects: " + keyword, sql, e);
        }
    }

    @Override
    public void update(Project project) throws DatabaseOperationException {
        final String sql = "UPDATE projects SET title = ?, description = ?, budget = ?, status = ?, " +
                "manager_id = ?, client_name = ?, tags = ?, is_active = ? WHERE id = ?";
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, project.getTitle());
                stmt.setString(2, project.getDescription());
                stmt.setDouble(3, project.getBudget());
                stmt.setString(4, project.getStatus().name());

                if (project.getManagerId() != null) {
                    stmt.setInt(5, project.getManagerId());
                } else {
                    stmt.setNull(5, java.sql.Types.INTEGER);
                }

                stmt.setString(6, project.getClientName());
                stmt.setString(7, project.getTagsFormatted());
                stmt.setInt(8, project.isActive() ? 1 : 0);
                stmt.setInt(9, project.getId());

                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update project ID: " + project.getId(), sql, e);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DatabaseOperationException {
        return softDelete(id);
    }

    @Override
    public boolean softDelete(int projectId) throws DatabaseOperationException {
        final String sql = "UPDATE projects SET is_active = 0 WHERE id = ?";
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, projectId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to soft-delete project ID: " + projectId, sql, e);
        }
    }

    @Override
    public long count() throws DatabaseOperationException {
        final String sql = "SELECT COUNT(*) FROM projects WHERE is_active = 1";
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
            throw new DatabaseOperationException("Failed to count active projects", sql, e);
        }
    }

    private Project mapResultSetToProject(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        double budget = rs.getDouble("budget");
        String statusStr = rs.getString("status");
        int mgrId = rs.getInt("manager_id");
        Integer managerId = rs.wasNull() ? null : mgrId;
        String clientName = rs.getString("client_name");
        String tagsRaw = rs.getString("tags");
        boolean active = rs.getInt("is_active") == 1;

        ProjectStatus status;
        try {
            status = ProjectStatus.valueOf(statusStr);
        } catch (Exception e) {
            status = ProjectStatus.PLANNING;
        }

        // Parse comma-separated tags into String[] array
        String[] tagsArray;
        if (tagsRaw != null && !tagsRaw.trim().isEmpty()) {
            tagsArray = tagsRaw.split(",\\s*");
        } else {
            tagsArray = new String[0];
        }

        Project p = new Project(id, title, description, budget, status, managerId, clientName, tagsArray);
        p.setActive(active);
        return p;
    }
}
