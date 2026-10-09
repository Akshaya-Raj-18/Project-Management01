package com.pms.model;

import java.time.LocalDateTime;
import java.util.Objects;
import com.pms.util.AppLogger;

/**
 * Abstract Base Entity serving as the root of the domain inheritance hierarchy.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 1: Abstract Classes, Wrapper classes (Integer), Primitive types,
 *   Interface implementation.
 * - Module 2: Protected access modifiers, Constructor call chaining order,
 *   Overloaded constructors, Dynamic use of 'this' keyword.
 */
public abstract class BaseEntity implements Identifiable, Auditable {

    // Module 2: Protected members accessible by subclasses
    protected Integer id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    // Module 2: Overloaded default constructor chaining to parameterized constructor
    public BaseEntity() {
        this(null);
    }

    // Module 2: Overloaded constructor with ID parameter
    public BaseEntity(Integer id) {
        // Module 2: Constructor call order logging demonstrating inheritance chain
        AppLogger.debug("[Constructor Order] BaseEntity constructor executing for ID: %s", id);

        // Module 2: Dynamic use of 'this'
        this.id = id;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
