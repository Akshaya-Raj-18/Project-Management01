package com.pms.model;

import java.util.Arrays;
import java.util.Vector;
import com.pms.util.AppLogger;

/**
 * Project Entity representing a managed client or internal software project.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 1: Wrapper types (Integer, Double), Arrays (String[] tags), Strings & StringBuilder,
 *   Vectors (Vector<String> representation), Primitive casting.
 * - Module 2: Inheritance (extends BaseEntity), Overloaded Constructors, Dynamic use of 'this',
 *   Method Overloading (addTag vs addTags), Member Inner Class (ProjectSummary accessing outer state),
 *   Static members, Final variables.
 */
public class Project extends BaseEntity {

    // Module 2: Static member tracking total instantiated projects
    private static long projectInstanceCounter = 0;

    // Fields
    private String title;
    private String description;
    private double budget;
    private ProjectStatus status;
    private Integer managerId;
    private String clientName;
    private String[] tags; // Module 1: Primitive array
    private boolean active;

    // Module 2: Overloaded Constructor 1 (Minimal) chaining to Overloaded Constructor 2
    public Project(String title, String description) {
        this(null, title, description, 0.0, ProjectStatus.PLANNING, null, "Internal", new String[0]);
    }

    // Module 2: Overloaded Constructor 2 (Budget & Manager)
    public Project(String title, String description, double budget, Integer managerId) {
        this(null, title, description, budget, ProjectStatus.PLANNING, managerId, "Internal", new String[0]);
    }

    // Module 2: Overloaded Constructor 3 (Full Parameter Set)
    public Project(Integer id, String title, String description, double budget, 
                   ProjectStatus status, Integer managerId, String clientName, String[] tags) {
        // Module 2: super(id) invoking BaseEntity constructor
        super(id);
        AppLogger.debug("[Constructor Order] Project constructor executing for project: %s", title);

        // Module 2: Dynamic use of 'this'
        this.title = title != null ? title.trim() : "";
        this.description = description != null ? description.trim() : "";
        this.budget = budget;
        this.status = status != null ? status : ProjectStatus.PLANNING;
        this.managerId = managerId;
        this.clientName = clientName != null ? clientName.trim() : "Internal";
        // Module 1: Array cloning / handling
        this.tags = (tags != null) ? Arrays.copyOf(tags, tags.length) : new String[0];
        this.active = true;

        // Module 2: Static member update
        projectInstanceCounter++;
    }

    // Module 2: Static member getter
    public static long getProjectInstanceCounter() {
        return projectInstanceCounter;
    }

    // Module 2: Method Overloading - addTag(String) vs addTag(String[])
    public void addTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return;
        }
        String cleanTag = tag.trim();
        // Module 1: Array resizing demonstration
        String[] newTags = Arrays.copyOf(this.tags, this.tags.length + 1);
        newTags[newTags.length - 1] = cleanTag;
        this.tags = newTags;
    }

    public void addTags(String[] newTagsToAdd) {
        if (newTagsToAdd == null || newTagsToAdd.length == 0) {
            return;
        }
        for (String tag : newTagsToAdd) {
            addTag(tag);
        }
    }

    /**
     * Module 1: Vector demonstration.
     * Converts tags array to a legacy Java Vector suitable for Swing UI controls.
     */
    public Vector<String> getTagsAsVector() {
        Vector<String> vector = new Vector<>();
        if (tags != null) {
            for (String tag : tags) {
                vector.add(tag);
            }
        }
        return vector;
    }

    /**
     * Comma-separated string of tags.
     * (Module 1: Strings and StringBuilder)
     */
    public String getTagsFormatted() {
        if (tags == null || tags.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tags.length; i++) {
            sb.append(tags[i]);
            if (i < tags.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String[] getTags() {
        return tags != null ? Arrays.copyOf(tags, tags.length) : new String[0];
    }

    public void setTags(String[] tags) {
        this.tags = (tags != null) ? Arrays.copyOf(tags, tags.length) : new String[0];
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - Budget: $%,.2f", id != null ? id : 0, title, status, budget);
    }

    /**
     * Module 2: Member Inner Class.
     * Has direct access to outer Project instance state, including private members.
     */
    public class ProjectSummary {
        private final int taskCount;
        private final double completionPercentage;

        public ProjectSummary(int taskCount, double completionPercentage) {
            this.taskCount = taskCount;
            this.completionPercentage = completionPercentage;
        }

        public String getFormattedSummary() {
            // Module 2: Accessing outer class state using Project.this syntax
            return String.format("Project '%s' [Status: %s] has %d tasks with %.1f%% overall progress. Budget: $%,.2f",
                    Project.this.title,
                    Project.this.status,
                    this.taskCount,
                    this.completionPercentage,
                    Project.this.budget);
        }

        public int getTaskCount() {
            return taskCount;
        }

        public double getCompletionPercentage() {
            return completionPercentage;
        }
    }
}
