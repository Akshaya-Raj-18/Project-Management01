package com.pms.gui.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;
import com.pms.model.Task;
import com.pms.model.TaskPriority;
import com.pms.model.TaskStatus;

/**
 * Adapter Design Pattern: Adapts Task domain entities to JTable AbstractTableModel.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 1: Vectors, Primitive casting, Wrapper types.
 * - Module 2: Recursion integration (renders dynamically calculated task completion percentage).
 * - Module 3: Adapter Design Pattern.
 * - Module 4: Swing Table Model Integration.
 */
public class TaskTableModelAdapter extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {
            "ID", "Task Title", "Project ID", "Priority", "Status", "Est. Hrs", "Logged Hrs", "Progress", "Depth"
    };

    private static final Class<?>[] COLUMN_CLASSES = {
            Integer.class, String.class, Integer.class, TaskPriority.class, TaskStatus.class,
            Double.class, Double.class, Double.class, Integer.class
    };

    private final List<Task> taskList;
    private final Vector<String> columnHeaders;

    public TaskTableModelAdapter() {
        this(new ArrayList<>());
    }

    public TaskTableModelAdapter(List<Task> tasks) {
        this.taskList = new ArrayList<>(tasks != null ? tasks : new ArrayList<>());
        
        // Module 1: Vector population
        this.columnHeaders = new Vector<>();
        for (String col : COLUMN_NAMES) {
            this.columnHeaders.add(col);
        }
    }

    public void setTasks(List<Task> tasks) {
        this.taskList.clear();
        if (tasks != null) {
            this.taskList.addAll(tasks);
        }
        fireTableDataChanged();
    }

    public Task getTaskAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < taskList.size()) {
            return taskList.get(rowIndex);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return taskList.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex >= 0 && columnIndex < columnHeaders.size()) {
            return columnHeaders.get(columnIndex);
        }
        return "";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex >= 0 && columnIndex < COLUMN_CLASSES.length) {
            return COLUMN_CLASSES[columnIndex];
        }
        return Object.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= taskList.size()) {
            return null;
        }

        Task task = taskList.get(rowIndex);

        // Module 1: Switch statement with primitive unboxing / wrapper types
        switch (columnIndex) {
            case 0:
                return task.getId();
            case 1:
                return task.getTitle();
            case 2:
                return task.getProjectId();
            case 3:
                return task.getPriority();
            case 4:
                return task.getStatus();
            case 5:
                return task.getEstimatedHours();
            case 6:
                return task.getLoggedHours();
            case 7:
                // Module 2: Evaluates recursive calculation dynamically!
                return task.calculateCompletionPercentage();
            case 8:
                // Module 2: Evaluates recursive dependency depth dynamically!
                return task.calculateDependencyDepth();
            default:
                return null;
        }
    }

    /**
     * Converts dataset into legacy Vector format.
     */
    public Vector<Vector<Object>> toVectorData() {
        Vector<Vector<Object>> data = new Vector<>();
        for (Task t : taskList) {
            Vector<Object> row = new Vector<>();
            row.add(t.getId());
            row.add(t.getTitle());
            row.add(t.getProjectId());
            row.add(t.getPriority());
            row.add(t.getStatus());
            row.add(t.getEstimatedHours());
            row.add(t.getLoggedHours());
            row.add(t.calculateCompletionPercentage());
            row.add(t.calculateDependencyDepth());
            data.add(row);
        }
        return data;
    }
}
