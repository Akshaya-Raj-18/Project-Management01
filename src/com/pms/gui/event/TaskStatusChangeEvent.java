package com.pms.gui.event;

import java.util.EventObject;
import com.pms.model.Task;
import com.pms.model.TaskStatus;

/**
 * Custom GUI Event triggered when a task's lifecycle status transitions.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 4: Custom Event Object, Event Delegation Model, Inheritance from EventObject.
 */
public class TaskStatusChangeEvent extends EventObject {

    private final Task task;
    private final TaskStatus oldStatus;
    private final TaskStatus newStatus;

    public TaskStatusChangeEvent(Object source, Task task, TaskStatus oldStatus, TaskStatus newStatus) {
        // Module 2 & 4: super(source) constructor call
        super(source);
        this.task = task;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public Task getTask() {
        return task;
    }

    public TaskStatus getOldStatus() {
        return oldStatus;
    }

    public TaskStatus getNewStatus() {
        return newStatus;
    }
}
