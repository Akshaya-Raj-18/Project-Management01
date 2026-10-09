package com.pms.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.pms.dao.ITaskDao;
import com.pms.exception.DatabaseOperationException;
import com.pms.exception.InvalidTaskStateException;
import com.pms.exception.TaskNotFoundException;
import com.pms.exception.UnauthorizedAccessException;
import com.pms.model.Permission;
import com.pms.model.Task;
import com.pms.model.TaskStatus;
import com.pms.model.User;
import com.pms.util.AppLogger;

/**
 * Task Service implementation managing task workflows, hierarchy trees, and validation.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 1: Switch statements, loops, type casting.
 * - Module 2: Method Overloading (searchTasks overloads), Recursion (populating sub-task trees),
 *   Dynamic Method Dispatch (initiator.hasPermission(permission)).
 * - Module 3: Custom Exception handling (InvalidTaskStateException, UnauthorizedAccessException),
 *   SOLID Principles.
 */
public class TaskService implements ITaskService {

    private final ITaskDao taskDao;

    public TaskService(ITaskDao taskDao) {
        this.taskDao = taskDao;
    }

    @Override
    public Task createTask(Task task, User initiator) 
            throws UnauthorizedAccessException, DatabaseOperationException {
        if (initiator == null || !initiator.hasPermission(Permission.CREATE_TASK)) {
            String username = initiator != null ? initiator.getUsername() : "ANONYMOUS";
            throw new UnauthorizedAccessException(username, "CREATE_TASK");
        }

        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }

        Task saved = taskDao.save(task);
        AppLogger.info("Task created: #%d '%s' for Project #%d by %s",
                saved.getId(), saved.getTitle(), saved.getProjectId(), initiator.getUsername());
        return saved;
    }

    @Override
    public void updateTask(Task task, User initiator) 
            throws UnauthorizedAccessException, DatabaseOperationException, TaskNotFoundException {
        if (task.getId() == null) {
            throw new TaskNotFoundException("Task ID is required for updating");
        }

        Optional<Task> existing = taskDao.findById(task.getId());
        if (!existing.isPresent()) {
            throw new TaskNotFoundException(task.getId());
        }

        taskDao.update(task);
        AppLogger.info("Task updated: #%d '%s' by %s", task.getId(), task.getTitle(),
                initiator != null ? initiator.getUsername() : "SYSTEM");
    }

    /**
     * Updates task status with strict lifecycle validation.
     * Throws InvalidTaskStateException if transition violates state workflow.
     */
    @Override
    public void updateTaskStatus(int taskId, TaskStatus newStatus, User initiator) 
            throws UnauthorizedAccessException, InvalidTaskStateException, TaskNotFoundException, DatabaseOperationException {
        if (initiator == null || !initiator.hasPermission(Permission.UPDATE_TASK_STATUS)) {
            String username = initiator != null ? initiator.getUsername() : "ANONYMOUS";
            throw new UnauthorizedAccessException(username, "UPDATE_TASK_STATUS");
        }

        Task task = getTaskById(taskId);
        TaskStatus currentStatus = task.getStatus();

        // Enforce state transition rules (Module 3: Custom Exception)
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidTaskStateException(
                    currentStatus.name(),
                    newStatus.name(),
                    String.format("Transition from %s to %s is not permitted in the engineering workflow.",
                            currentStatus.getLabel(), newStatus.getLabel()));
        }

        taskDao.updateStatus(taskId, newStatus);
        AppLogger.info("Task #%d status changed: %s -> %s by %s",
                taskId, currentStatus, newStatus, initiator.getUsername());
    }

    @Override
    public void logHours(int taskId, double hours, User initiator) 
            throws UnauthorizedAccessException, TaskNotFoundException, DatabaseOperationException {
        if (initiator == null || !initiator.hasPermission(Permission.LOG_HOURS)) {
            String username = initiator != null ? initiator.getUsername() : "ANONYMOUS";
            throw new UnauthorizedAccessException(username, "LOG_HOURS");
        }

        if (hours <= 0) {
            throw new IllegalArgumentException("Logged hours must be greater than zero");
        }

        // Verify task existence
        getTaskById(taskId);

        taskDao.logHours(taskId, hours);
        AppLogger.info("Logged %.2f hours on task #%d by user '%s'", hours, taskId, initiator.getUsername());
    }

    @Override
    public void deleteTask(int taskId, User initiator) 
            throws UnauthorizedAccessException, DatabaseOperationException, TaskNotFoundException {
        if (initiator == null || !initiator.hasPermission(Permission.DELETE_TASK)) {
            String username = initiator != null ? initiator.getUsername() : "ANONYMOUS";
            throw new UnauthorizedAccessException(username, "DELETE_TASK");
        }

        getTaskById(taskId);
        taskDao.softDelete(taskId);
        AppLogger.info("Task #%d deleted by %s", taskId, initiator.getUsername());
    }

    @Override
    public Task getTaskById(int taskId) throws TaskNotFoundException, DatabaseOperationException {
        return taskDao.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    @Override
    public List<Task> getAllTasks() throws DatabaseOperationException {
        return taskDao.findAll();
    }

    @Override
    public List<Task> getTasksByProject(int projectId) throws DatabaseOperationException {
        return taskDao.findByProjectId(projectId);
    }

    /**
     * Builds and returns root tasks with all child sub-tasks recursively populated in memory.
     * Demonstrates Module 2: Recursion.
     */
    @Override
    public List<Task> buildTaskHierarchy(int projectId) throws DatabaseOperationException {
        List<Task> topTasks = taskDao.findTopLevelTasksByProject(projectId);
        for (Task rootTask : topTasks) {
            recursivelyAttachSubTasks(rootTask);
        }
        return topTasks;
    }

    /**
     * Recursive helper to build composite task hierarchy.
     */
    private void recursivelyAttachSubTasks(Task parent) throws DatabaseOperationException {
        if (parent == null || parent.getId() == null) {
            return;
        }
        parent.clearSubTasks();
        List<Task> directChildren = taskDao.findSubTasks(parent.getId());
        for (Task child : directChildren) {
            parent.addSubTask(child);
            // Recursive invocation down the child branches
            recursivelyAttachSubTasks(child);
        }
    }

    // =========================================================================
    // MODULE 2: METHOD OVERLOADING (Core Syllabus Requirement)
    // =========================================================================

    /**
     * Method Overload 1: Search by text query string.
     */
    @Override
    public List<Task> searchTasks(String keyword) throws DatabaseOperationException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return taskDao.findAll();
        }
        return taskDao.search(keyword.trim());
    }

    /**
     * Method Overload 2: Search/filter by TaskStatus enum.
     */
    @Override
    public List<Task> searchTasks(TaskStatus status) throws DatabaseOperationException {
        if (status == null) {
            return taskDao.findAll();
        }
        return taskDao.findByStatus(status);
    }

    /**
     * Method Overload 3: Search/filter by both Project ID and TaskStatus enum.
     */
    @Override
    public List<Task> searchTasks(int projectId, TaskStatus status) throws DatabaseOperationException {
        List<Task> projectTasks = taskDao.findByProjectId(projectId);
        if (status == null) {
            return projectTasks;
        }

        List<Task> filtered = new ArrayList<>();
        for (Task t : projectTasks) {
            if (t.getStatus() == status) {
                filtered.add(t);
            }
        }
        return filtered;
    }

    @Override
    public long getTotalTaskCount() throws DatabaseOperationException {
        return taskDao.count();
    }

    @Override
    public long getCompletedTaskCount() throws DatabaseOperationException {
        return taskDao.findByStatus(TaskStatus.COMPLETED).size();
    }
}
