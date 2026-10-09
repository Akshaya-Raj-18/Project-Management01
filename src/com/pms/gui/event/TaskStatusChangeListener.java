package com.pms.gui.event;

import java.util.EventListener;

/**
 * Custom Event Listener Interface for task status transition events.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 4: Event Delegation Model, Custom Listener Interface inheriting from EventListener.
 */
public interface TaskStatusChangeListener extends EventListener {

    /**
     * Invoked when a task's status has been modified.
     */
    void onTaskStatusChanged(TaskStatusChangeEvent event);
}
