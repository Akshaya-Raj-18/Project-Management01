package com.pms.dao;

import java.util.List;
import com.pms.exception.DatabaseOperationException;
import com.pms.model.Project;
import com.pms.model.ProjectStatus;

/**
 * Data Access Object contract for Project operations.
 * (Module 3: Custom Interface Inheritance)
 */
public interface IProjectDao extends Repository<Project, Integer> {

    /**
     * Retrieve projects managed by a specific user.
     */
    List<Project> findByManagerId(int managerId) throws DatabaseOperationException;

    /**
     * Retrieve projects filtered by status.
     */
    List<Project> findByStatus(ProjectStatus status) throws DatabaseOperationException;

    /**
     * Soft-delete a project (mark inactive / deleted without purging history).
     */
    boolean softDelete(int projectId) throws DatabaseOperationException;

    /**
     * Filter projects by tag keyword.
     */
    List<Project> findByTag(String tag) throws DatabaseOperationException;
}
