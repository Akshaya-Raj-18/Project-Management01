package com.pms.service;

import java.util.List;
import java.util.Optional;
import com.pms.dao.IProjectDao;
import com.pms.dao.ITaskDao;
import com.pms.exception.DatabaseOperationException;
import com.pms.exception.ProjectNotFoundException;
import com.pms.exception.UnauthorizedAccessException;
import com.pms.model.Permission;
import com.pms.model.Project;
import com.pms.model.ProjectStatus;
import com.pms.model.Task;
import com.pms.model.User;
import com.pms.util.AppLogger;

/**
 * Project Service implementation enforcing SOLID and Role-Based Access Control.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 1: Primitive casting ((double)), Control flow (if-else, loops).
 * - Module 2: Dynamic Method Dispatch (initiator.hasPermission(permission)),
 *   Recursion integration (evaluating hierarchical task trees).
 * - Module 3: Custom Exception handling, SOLID principles (SRP, DIP).
 */
public class ProjectService implements IProjectService {

    private final IProjectDao projectDao;
    private final ITaskDao taskDao;

    public ProjectService(IProjectDao projectDao, ITaskDao taskDao) {
        this.projectDao = projectDao;
        this.taskDao = taskDao;
    }

    @Override
    public Project createProject(Project project, User initiator) 
            throws UnauthorizedAccessException, DatabaseOperationException {
        if (initiator == null || !initiator.hasPermission(Permission.CREATE_PROJECT)) {
            String username = initiator != null ? initiator.getUsername() : "ANONYMOUS";
            throw new UnauthorizedAccessException(username, "CREATE_PROJECT");
        }

        if (project.getTitle() == null || project.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Project title cannot be empty");
        }

        Project saved = projectDao.save(project);
        AppLogger.info("Project created: '%s' (ID: %d) by %s", saved.getTitle(), saved.getId(), initiator.getUsername());
        return saved;
    }

    @Override
    public void updateProject(Project project, User initiator) 
            throws UnauthorizedAccessException, DatabaseOperationException, ProjectNotFoundException {
        if (initiator == null || !initiator.hasPermission(Permission.EDIT_PROJECT)) {
            String username = initiator != null ? initiator.getUsername() : "ANONYMOUS";
            throw new UnauthorizedAccessException(username, "EDIT_PROJECT");
        }

        if (project.getId() == null) {
            throw new ProjectNotFoundException("Project ID must not be null for update");
        }

        Optional<Project> existing = projectDao.findById(project.getId());
        if (!existing.isPresent()) {
            throw new ProjectNotFoundException(project.getId());
        }

        projectDao.update(project);
        AppLogger.info("Project updated: ID %d by %s", project.getId(), initiator.getUsername());
    }

    @Override
    public void deleteProject(int projectId, User initiator) 
            throws UnauthorizedAccessException, DatabaseOperationException, ProjectNotFoundException {
        // Module 2: Dynamic method dispatch - checks role-based permission
        if (initiator == null || !initiator.hasPermission(Permission.DELETE_PROJECT)) {
            String username = initiator != null ? initiator.getUsername() : "ANONYMOUS";
            throw new UnauthorizedAccessException(username, "DELETE_PROJECT");
        }

        Optional<Project> existing = projectDao.findById(projectId);
        if (!existing.isPresent()) {
            throw new ProjectNotFoundException(projectId);
        }

        boolean deleted = projectDao.softDelete(projectId);
        if (deleted) {
            AppLogger.info("Project ID %d successfully soft-deleted by %s", projectId, initiator.getUsername());
        }
    }

    @Override
    public List<Project> getAllProjects() throws DatabaseOperationException {
        return projectDao.findAll();
    }

    @Override
    public Project getProjectById(int id) throws ProjectNotFoundException, DatabaseOperationException {
        return projectDao.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
    }

    @Override
    public List<Project> searchProjects(String keyword) throws DatabaseOperationException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return projectDao.findAll();
        }
        return projectDao.search(keyword.trim());
    }

    @Override
    public List<Project> getProjectsByStatus(ProjectStatus status) throws DatabaseOperationException {
        return projectDao.findByStatus(status);
    }

    /**
     * Dynamically calculates project overall completion percentage by recursively
     * aggregating completion metrics across the project's task trees.
     * 
     * Demonstrates Module 1: Primitive casting ((double)).
     * Demonstrates Module 2: Recursion.
     */
    @Override
    public double calculateProjectCompletion(int projectId) 
            throws ProjectNotFoundException, DatabaseOperationException {
        Project project = getProjectById(projectId);
        List<Task> topTasks = taskDao.findTopLevelTasksByProject(projectId);

        if (topTasks.isEmpty()) {
            return project.getStatus() == ProjectStatus.COMPLETED ? 100.0 : 0.0;
        }

        // Build composite hierarchy for each top-level task
        double sumPercentage = 0.0;
        for (Task topTask : topTasks) {
            populateSubTasksRecursively(topTask);
            // Polymorphic recursive calculation on Task composite
            sumPercentage += topTask.calculateCompletionPercentage();
        }

        // Module 1: Primitive double casting
        double aggregate = sumPercentage / (double) topTasks.size();
        return Math.round(aggregate * 10.0) / 10.0;
    }

    /**
     * Helper to recursively attach child sub-tasks to build full task trees.
     */
    private void populateSubTasksRecursively(Task parentTask) throws DatabaseOperationException {
        if (parentTask == null || parentTask.getId() == null) {
            return;
        }
        List<Task> children = taskDao.findSubTasks(parentTask.getId());
        for (Task child : children) {
            parentTask.addSubTask(child);
            // Recursive descent down the hierarchy
            populateSubTasksRecursively(child);
        }
    }

    @Override
    public long getTotalProjectCount() throws DatabaseOperationException {
        return projectDao.count();
    }
}
