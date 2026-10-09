package com.pms.service;

import java.util.List;
import com.pms.exception.DatabaseOperationException;
import com.pms.exception.ProjectNotFoundException;
import com.pms.exception.UnauthorizedAccessException;
import com.pms.model.Project;
import com.pms.model.ProjectStatus;
import com.pms.model.User;

/**
 * Service Contract for Project lifecycle management.
 * (Module 3: Interface Segregation and Custom Exceptions)
 */
public interface IProjectService {

    Project createProject(Project project, User initiator)
            throws UnauthorizedAccessException, DatabaseOperationException;

    void updateProject(Project project, User initiator)
            throws UnauthorizedAccessException, DatabaseOperationException, ProjectNotFoundException;

    void deleteProject(int projectId, User initiator)
            throws UnauthorizedAccessException, DatabaseOperationException, ProjectNotFoundException;

    List<Project> getAllProjects() throws DatabaseOperationException;

    Project getProjectById(int id) throws ProjectNotFoundException, DatabaseOperationException;

    List<Project> searchProjects(String keyword) throws DatabaseOperationException;

    List<Project> getProjectsByStatus(ProjectStatus status) throws DatabaseOperationException;

    double calculateProjectCompletion(int projectId)
            throws ProjectNotFoundException, DatabaseOperationException;

    long getTotalProjectCount() throws DatabaseOperationException;
}
