package com.pms.model;

import java.time.LocalDateTime;

/**
 * Interface contract for domain entities tracking creation and update timestamps.
 * (Module 1 & 3: Interfaces and Interface Segregation Principle)
 */
public interface Auditable {
    LocalDateTime getCreatedAt();
    void setCreatedAt(LocalDateTime createdAt);
    
    LocalDateTime getUpdatedAt();
    void setUpdatedAt(LocalDateTime updatedAt);
}
