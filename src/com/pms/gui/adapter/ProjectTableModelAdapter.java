package com.pms.gui.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;
import com.pms.model.Project;
import com.pms.model.ProjectStatus;

/**
 * Adapter Design Pattern: Adapts domain Project entities to Swing JTable AbstractTableModel.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 1: Vectors (Vector<Vector<Object>>, Vector<String>), Primitive casting.
 * - Module 2: Dynamic use of 'this', Method overriding (@Override).
 * - Module 3: Adapter Design Pattern.
 * - Module 4: Swing Table Model Integration.
 */
public class ProjectTableModelAdapter extends AbstractTableModel {

    // Column definitions
    private static final String[] COLUMN_NAMES = {
            "ID", "Project Title", "Client", "Budget ($)", "Status", "Tags"
    };

    private static final Class<?>[] COLUMN_CLASSES = {
            Integer.class, String.class, String.class, Double.class, ProjectStatus.class, String.class
    };

    private final List<Project> projectList;
    // Module 1: Vector usage for Swing model compatibility
    private final Vector<String> columnIdentifiers;

    public ProjectTableModelAdapter() {
        this(new ArrayList<>());
    }

    public ProjectTableModelAdapter(List<Project> projects) {
        this.projectList = new ArrayList<>(projects != null ? projects : new ArrayList<>());
        
        // Module 1: Initializing Vector
        this.columnIdentifiers = new Vector<>();
        for (String col : COLUMN_NAMES) {
            this.columnIdentifiers.add(col);
        }
    }

    public void setProjects(List<Project> projects) {
        this.projectList.clear();
        if (projects != null) {
            this.projectList.addAll(projects);
        }
        // Notify JTable views of data alteration
        fireTableDataChanged();
    }

    public Project getProjectAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < projectList.size()) {
            return projectList.get(rowIndex);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return projectList.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex >= 0 && columnIndex < columnIdentifiers.size()) {
            return columnIdentifiers.get(columnIndex);
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
        if (rowIndex < 0 || rowIndex >= projectList.size()) {
            return null;
        }

        Project project = projectList.get(rowIndex);

        // Module 1: Switch control statement
        switch (columnIndex) {
            case 0:
                return project.getId();
            case 1:
                return project.getTitle();
            case 2:
                return project.getClientName();
            case 3:
                return project.getBudget();
            case 4:
                return project.getStatus();
            case 5:
                return project.getTagsFormatted();
            default:
                return null;
        }
    }

    /**
     * Converts underlying project data into nested Vectors for legacy Swing interoperability.
     * (Module 1: Vector<Vector<Object>>)
     */
    public Vector<Vector<Object>> toVectorData() {
        Vector<Vector<Object>> data = new Vector<>();
        for (Project p : projectList) {
            Vector<Object> row = new Vector<>();
            row.add(p.getId());
            row.add(p.getTitle());
            row.add(p.getClientName());
            row.add(p.getBudget());
            row.add(p.getStatus());
            row.add(p.getTagsFormatted());
            data.add(row);
        }
        return data;
    }
}
