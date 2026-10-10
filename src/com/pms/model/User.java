package com.pms.model;

import java.util.Objects;
import com.pms.util.AppLogger;

/**
 * Abstract User superclass representing an authenticated system user.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 1: Abstract Classes, Strings and StringBuilder, Wrapper types.
 * - Module 2: Inheritance (Superclass), Protected members, super keyword,
 *   Constructor chaining, Method overriding, Dynamic Method Dispatch,
 *   Static nested classes (Credentials).
 */
public abstract class User extends BaseEntity {

    // Module 2: Protected instance variables accessible by Manager and Developer subclasses
    protected String username;
    protected String passwordHash;
    protected String fullName;
    protected String email;
    protected UserRole role;
    protected boolean active;

    // Module 2: Overloaded constructor chaining to superclass BaseEntity
    public User() {
        super();
        this.active = true;
    }

    public User(Integer id, String username, String passwordHash, String fullName, String email, UserRole role) {
        // Module 2: Calling superclass constructor using 'super'
        super(id);
        AppLogger.debug("[Constructor Order] User constructor executing for username: %s", username);

        // Module 2: Dynamic use of 'this'
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.active = true;
    }

    /**
     * Module 2: Abstract method enforcing Dynamic Method Dispatch.
     * Each subclass (Manager, Developer) provides polymorphic authorization logic.
     *
     * @param permission The requested action permission
     * @return true if authorized, false otherwise
     */
    public abstract boolean hasPermission(Permission permission);

    /**
     * Module 2: Abstract method for polymorphic role description retrieval.
     */
    public abstract String getRoleDescription();

    /**
     * Module 2: Dynamic Method Dispatch in action:
     * This method calls the polymorphic getRoleDescription() which executes
     * the subclass implementation at runtime based on the actual object instance.
     */
    public String getProfileBadgeText() {
        StringBuilder sb = new StringBuilder();
        sb.append(fullName)
          .append(" (")
          .append(username)
          .append(") - ")
          .append(getRoleDescription());
        return sb.toString();
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // Module 2: Method Overriding
    @Override
    public String toString() {
        return String.format("[%s] %s (%s)", role != null ? role.name() : "USER", fullName, username);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), username);
    }

    /**
     * Module 2: Static Nested Class (Inner Class architecture)
     * Encapsulates authentication credentials as a self-contained value object.
     */
    public static class Credentials {
        private final String username;
        private final String rawPassword;

        public Credentials(String username, String rawPassword) {
            this.username = username != null ? username.trim() : "";
            this.rawPassword = rawPassword != null ? rawPassword : "";
        }

        public String getUsername() {
            return username;
        }

        public String getRawPassword() {
            return rawPassword;
        }

        public boolean isValid() {
            return !username.isEmpty() && !rawPassword.isEmpty();
        }
    }
}
