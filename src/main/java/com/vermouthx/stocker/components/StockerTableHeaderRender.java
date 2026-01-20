package com.vermouthx.stocker.components;

import com.vermouthx.stocker.enums.StockerSortState;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class StockerTableHeaderRender implements TableCellRenderer {

    private final TableCellRenderer renderer;
    private int sortColumn = -1;
    private StockerSortState sortState = StockerSortState.NONE;

    public StockerTableHeaderRender(JTable table) {
        renderer = table.getTableHeader().getDefaultRenderer();
    }

    public void setSortState(int column, StockerSortState state) {
        this.sortColumn = column;
        this.sortState = state;
    }

    public int getSortColumn() {
        return sortColumn;
    }

    public StockerSortState getSortState() {
        return sortState;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (component instanceof JLabel) {
            JLabel label = (JLabel) component;
            label.setHorizontalAlignment(SwingConstants.CENTER);
            
            // Add sort indicator
            if (column == sortColumn && sortState != StockerSortState.NONE) {
                String sortIndicator = sortState == StockerSortState.ASCENDING ? " ▲" : " ▼";
                label.setText(value + sortIndicator);
            } else {
                label.setText(value != null ? value.toString() : "");
            }
        }
        return component;
    }
}
