package com.pms.model;

/**
 * Interface contract for domain entities that possess a unique identifier.
 * (Module 1 & 3: Interfaces and Abstraction)
 */
public interface Identifiable {
    Integer getId();
    void setId(Integer id);
}
