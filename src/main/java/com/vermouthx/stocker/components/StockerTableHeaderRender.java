package com.vermouthx.stocker.components;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class StockerTableHeaderRender implements TableCellRenderer {

    private final TableCellRenderer renderer;

    public StockerTableHeaderRender(JTable table) {
        renderer = table.getTableHeader().getDefaultRenderer();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}
