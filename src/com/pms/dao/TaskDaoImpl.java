package com.pms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.pms.exception.DatabaseOperationException;
import com.pms.model.Task;
import com.pms.model.TaskPriority;
import com.pms.model.TaskStatus;
import com.pms.util.AppLogger;

/**
 * JDBC Implementation of ITaskDao.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 3: Exception Handling, Transactions, Nested Try blocks.
 * - Module 4: Dynamic SQL Transactions (Atomic commit/rollback for work logging),
 *   JDBC CRUD operations, PreparedStatement, ResultSet.
 */
public class TaskDaoImpl implements ITaskDao {

    private final DatabaseConnectionManager connectionManager;

    public TaskDaoImpl() {
        this(DatabaseConnectionManager.getInstance());
    }

    public TaskDaoImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Task save(Task task) throws DatabaseOperationException {
        final String sql = "INSERT INTO tasks (project_id, title, description, assigned_user_id, " +
                "parent_task_id, status, priority, estimated_hours, logged_hours, is_deleted) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, task.getProjectId());
                stmt.setString(2, task.getTitle());
                stmt.setString(3, task.getDescription());

                if (task.getAssignedUserId() != null) {
                    stmt.setInt(4, task.getAssignedUserId());
                } else {
                    stmt.setNull(4, java.sql.Types.INTEGER);
                }

                if (task.getParentTaskId() != null) {
                    stmt.setInt(5, task.getParentTaskId());
                } else {
                    stmt.setNull(5, java.sql.Types.INTEGER);
                }

                stmt.setString(6, task.getStatus().name());
                stmt.setString(7, task.getPriority().name());
                stmt.setDouble(8, task.getEstimatedHours());
                stmt.setDouble(9, task.getLoggedHours());
                stmt.setInt(10, task.isDeleted() ? 1 : 0);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            task.setId(keys.getInt(1));
                        }
                    }
                }
                return task;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save task: " + task.getTitle(), sql, e);
        }
    }

    @Override
    public Optional<Task> findById(Integer id) throws DatabaseOperationException {
        final String sql = "SELECT * FROM tasks WHERE id = ? AND is_deleted = 0";
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToTask(rs));
                    }
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error finding task by ID: " + id, sql, e);
        }
    }

    @Override
    public List<Task> findAll() throws DatabaseOperationException {
        final String sql = "SELECT * FROM tasks WHERE is_deleted = 0 ORDER BY id DESC";
        List<Task> list = new ArrayList<>();
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTask(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching all tasks", sql, e);
        }
    }

    @Override
    public List<Task> findByProjectId(int projectId) throws DatabaseOperationException {
        final String sql = "SELECT * FROM tasks WHERE project_id = ? AND is_deleted = 0 ORDER BY id ASC";
        List<Task> list = new ArrayList<>();
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, projectId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToTask(rs));
                    }
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching tasks for project ID: " + projectId, sql, e);
        }
    }

    @Override
    public List<Task> findTopLevelTasksByProject(int projectId) throws DatabaseOperationException {
        final String sql = "SELECT * FROM tasks WHERE project_id = ? AND parent_task_id IS NULL AND is_deleted = 0 ORDER BY id ASC";
        List<Task> list = new ArrayList<>();
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, projectId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToTask(rs));
                    }
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching top-level tasks for project: " + projectId, sql, e);
        }
    }

    @Override
    public List<Task> findSubTasks(int parentTaskId) throws DatabaseOperationException {
        final String sql = "SELECT * FROM tasks WHERE parent_task_id = ? AND is_deleted = 0 ORDER BY id ASC";
        List<Task> list = new ArrayList<>();
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, parentTaskId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToTask(rs));
                    }
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching sub-tasks for parent ID: " + parentTaskId, sql, e);
        }
    }

    @Override
    public List<Task> findByAssignedUserId(int userId) throws DatabaseOperationException {
        final String sql = "SELECT * FROM tasks WHERE assigned_user_id = ? AND is_deleted = 0 ORDER BY id DESC";
        List<Task> list = new ArrayList<>();
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToTask(rs));
                    }
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching tasks assigned to user ID: " + userId, sql, e);
        }
    }

    @Override
    public List<Task> findByStatus(TaskStatus status) throws DatabaseOperationException {
        final String sql = "SELECT * FROM tasks WHERE status = ? AND is_deleted = 0 ORDER BY id DESC";
        List<Task> list = new ArrayList<>();
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status.name());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToTask(rs));
                    }
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching tasks by status: " + status, sql, e);
        }
    }

    @Override
    public void update(Task task) throws DatabaseOperationException {
        final String sql = "UPDATE tasks SET project_id = ?, title = ?, description = ?, " +
                "assigned_user_id = ?, parent_task_id = ?, status = ?, priority = ?, " +
                "estimated_hours = ?, logged_hours = ?, is_deleted = ? WHERE id = ?";
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, task.getProjectId());
                stmt.setString(2, task.getTitle());
                stmt.setString(3, task.getDescription());

                if (task.getAssignedUserId() != null) {
                    stmt.setInt(4, task.getAssignedUserId());
                } else {
                    stmt.setNull(4, java.sql.Types.INTEGER);
                }

                if (task.getParentTaskId() != null) {
                    stmt.setInt(5, task.getParentTaskId());
                } else {
                    stmt.setNull(5, java.sql.Types.INTEGER);
                }

                stmt.setString(6, task.getStatus().name());
                stmt.setString(7, task.getPriority().name());
                stmt.setDouble(8, task.getEstimatedHours());
                stmt.setDouble(9, task.getLoggedHours());
                stmt.setInt(10, task.isDeleted() ? 1 : 0);
                stmt.setInt(11, task.getId());

                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating task ID: " + task.getId(), sql, e);
        }
    }

    @Override
    public void updateStatus(int taskId, TaskStatus newStatus) throws DatabaseOperationException {
        final String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newStatus.name());
                stmt.setInt(2, taskId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating status for task ID: " + taskId, sql, e);
        }
    }

    /**
     * Module 4: Dynamic SQL Transaction with commit and rollback.
     * Atomically adds work log record and increments logged hours.
     */
    @Override
    public void logHours(int taskId, double hoursLogged) throws DatabaseOperationException {
        final String updateTaskSql = "UPDATE tasks SET logged_hours = logged_hours + ? WHERE id = ?";
        final String insertLogSql = "INSERT INTO task_logs (task_id, hours_logged, log_date, notes) VALUES (?, ?, ?, ?)";

        connectionManager.beginTransaction();
        try {
            Connection conn = connectionManager.getConnection();
            
            // 1. Update cumulative hours
            try (PreparedStatement stmt1 = conn.prepareStatement(updateTaskSql)) {
                stmt1.setDouble(1, hoursLogged);
                stmt1.setInt(2, taskId);
                stmt1.executeUpdate();
            }

            // 2. Insert audit log entry
            try (PreparedStatement stmt2 = conn.prepareStatement(insertLogSql)) {
                stmt2.setInt(1, taskId);
                stmt2.setDouble(2, hoursLogged);
                stmt2.setString(3, LocalDateTime.now().toString());
                stmt2.setString(4, "Progress logged via PMS Interactive GUI");
                stmt2.executeUpdate();
            }

            // Commit atomic transaction
            connectionManager.commitTransaction();
            AppLogger.info("Transaction succeeded: Logged %.2f hours for task ID %d", hoursLogged, taskId);
        } catch (Exception e) {
            connectionManager.rollbackTransaction();
            throw new DatabaseOperationException("Failed to log hours due to transaction error", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DatabaseOperationException {
        return softDelete(id);
    }

    @Override
    public boolean softDelete(int taskId) throws DatabaseOperationException {
        final String sql = "UPDATE tasks SET is_deleted = 1 WHERE id = ?";
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, taskId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error soft-deleting task ID: " + taskId, sql, e);
        }
    }

    @Override
    public List<Task> search(String keyword) throws DatabaseOperationException {
        final String sql = "SELECT * FROM tasks WHERE is_deleted = 0 AND (title LIKE ? OR description LIKE ?)";
        List<Task> list = new ArrayList<>();
        try {
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                String pattern = "%" + keyword + "%";
                stmt.setString(1, pattern);
                stmt.setString(2, pattern);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToTask(rs));
                    }
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error searching tasks with keyword: " + keyword, sql, e);
        }
    }

    @Override
    public long count() throws DatabaseOperationException {
        final String sql = "SELECT COUNT(*) FROM tasks WHERE is_deleted = 0";
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
            throw new DatabaseOperationException("Error counting tasks", sql, e);
        }
    }

    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int projectId = rs.getInt("project_id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        
        int assignedId = rs.getInt("assigned_user_id");
        Integer assignedUserId = rs.wasNull() ? null : assignedId;

        int parentId = rs.getInt("parent_task_id");
        Integer parentTaskId = rs.wasNull() ? null : parentId;

        String statusStr = rs.getString("status");
        String priorityStr = rs.getString("priority");
        double estimatedHours = rs.getDouble("estimated_hours");
        double loggedHours = rs.getDouble("logged_hours");
        boolean deleted = rs.getInt("is_deleted") == 1;

        TaskStatus status;
        try {
            status = TaskStatus.valueOf(statusStr);
        } catch (Exception e) {
            status = TaskStatus.TODO;
        }

        TaskPriority priority;
        try {
            priority = TaskPriority.valueOf(priorityStr);
        } catch (Exception e) {
            priority = TaskPriority.MEDIUM;
        }

        Task task = new Task(id, projectId, title, description, assignedUserId,
                parentTaskId, status, priority, estimatedHours, loggedHours);
        task.setDeleted(deleted);
        return task;
    }
}
