package com.pms.dao;

import java.util.List;
import com.pms.exception.DatabaseOperationException;
import com.pms.model.Task;
import com.pms.model.TaskStatus;

/**
 * Data Access Object contract for Task and Sub-task operations.
 * (Module 3: Custom Interface Inheritance)
 */
public interface ITaskDao extends Repository<Task, Integer> {

    /**
     * Retrieve all top-level and sub-tasks for a given project.
     */
    List<Task> findByProjectId(int projectId) throws DatabaseOperationException;

    /**
     * Retrieve top-level tasks for a given project (where parent_task_id IS NULL).
     */
    List<Task> findTopLevelTasksByProject(int projectId) throws DatabaseOperationException;

    /**
     * Retrieve direct child sub-tasks of a parent task.
     */
    List<Task> findSubTasks(int parentTaskId) throws DatabaseOperationException;

    /**
     * Retrieve tasks assigned to a specific developer.
     */
    List<Task> findByAssignedUserId(int userId) throws DatabaseOperationException;

    /**
     * Retrieve tasks filtered by status.
     */
    List<Task> findByStatus(TaskStatus status) throws DatabaseOperationException;

    /**
     * Update task status.
     */
    void updateStatus(int taskId, TaskStatus newStatus) throws DatabaseOperationException;

    /**
     * Log work hours against a task.
     */
    void logHours(int taskId, double hoursLogged) throws DatabaseOperationException;

    /**
     * Soft-delete a task.
     */
    boolean softDelete(int taskId) throws DatabaseOperationException;
}
