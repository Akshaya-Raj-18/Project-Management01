package com.pms.service;

import java.util.List;
import com.pms.exception.DatabaseOperationException;
import com.pms.exception.InvalidTaskStateException;
import com.pms.exception.TaskNotFoundException;
import com.pms.exception.UnauthorizedAccessException;
import com.pms.model.Task;
import com.pms.model.TaskStatus;
import com.pms.model.User;

/**
 * Service Contract for Task, Sub-task and workflow management.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 2: Method Overloading (multiple searchTasks signatures).
 * - Module 3: Interface Segregation, Custom Exceptions (UnauthorizedAccessException,
 *   InvalidTaskStateException, TaskNotFoundException).
 */
public interface ITaskService {

    Task createTask(Task task, User initiator) 
            throws UnauthorizedAccessException, DatabaseOperationException;

    void updateTask(Task task, User initiator) 
            throws UnauthorizedAccessException, DatabaseOperationException, TaskNotFoundException;

    void updateTaskStatus(int taskId, TaskStatus newStatus, User initiator) 
            throws UnauthorizedAccessException, InvalidTaskStateException, TaskNotFoundException, DatabaseOperationException;

    void logHours(int taskId, double hours, User initiator) 
            throws UnauthorizedAccessException, TaskNotFoundException, DatabaseOperationException;

    void deleteTask(int taskId, User initiator) 
            throws UnauthorizedAccessException, DatabaseOperationException, TaskNotFoundException;

    Task getTaskById(int taskId) throws TaskNotFoundException, DatabaseOperationException;

    List<Task> getAllTasks() throws DatabaseOperationException;

    List<Task> getTasksByProject(int projectId) throws DatabaseOperationException;

    /**
     * Builds and returns top-level tasks with child sub-tasks recursively populated in memory.
     */
    List<Task> buildTaskHierarchy(int projectId) throws DatabaseOperationException;

    // Module 2: Method Overloading 1 - search by text query
    List<Task> searchTasks(String keyword) throws DatabaseOperationException;

    // Module 2: Method Overloading 2 - filter by status
    List<Task> searchTasks(TaskStatus status) throws DatabaseOperationException;

    // Module 2: Method Overloading 3 - filter by project and status
    List<Task> searchTasks(int projectId, TaskStatus status) throws DatabaseOperationException;

    long getTotalTaskCount() throws DatabaseOperationException;

    long getCompletedTaskCount() throws DatabaseOperationException;
}
