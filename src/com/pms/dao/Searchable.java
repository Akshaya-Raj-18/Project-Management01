package com.pms.dao;

import java.util.List;
import com.pms.exception.DatabaseOperationException;

/**
 * Interface contract for entities supporting text-based search queries.
 * (Module 1 & 3: Interfaces and Generics)
 *
 * @param <T> Domain entity type
 */
public interface Searchable<T> {

    /**
     * Search entities matching the keyword query.
     *
     * @param keyword Search term
     * @return List of matching entity instances
     * @throws DatabaseOperationException if query fails
     */
    List<T> search(String keyword) throws DatabaseOperationException;
}
