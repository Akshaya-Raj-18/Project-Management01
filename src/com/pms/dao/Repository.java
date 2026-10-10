package com.pms.dao;

import java.util.List;
import java.util.Optional;
import com.pms.exception.DatabaseOperationException;

/**
 * Generic Data Access Object (DAO) Repository contract.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 3: Packages & Interfaces, Custom Interface Inheritance
 *   (Repository<T, ID> extends Searchable<T>), Generics, Interface References.
 * - Module 3: Exception throws declarations.
 *
 * @param <T>  Domain entity type
 * @param <ID> Primary key wrapper type
 */
public interface Repository<T, ID> extends Searchable<T> {

    /**
     * Persist a new entity.
     */
    T save(T entity) throws DatabaseOperationException;

    /**
     * Locate entity by its primary key.
     */
    Optional<T> findById(ID id) throws DatabaseOperationException;

    /**
     * Retrieve all active entities.
     */
    List<T> findAll() throws DatabaseOperationException;

    /**
     * Update an existing entity.
     */
    void update(T entity) throws DatabaseOperationException;

    /**
     * Delete an entity by ID (or soft-delete).
     */
    boolean deleteById(ID id) throws DatabaseOperationException;

    /**
     * Count active records.
     */
    long count() throws DatabaseOperationException;
}
