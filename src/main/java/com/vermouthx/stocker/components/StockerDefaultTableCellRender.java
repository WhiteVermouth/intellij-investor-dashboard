package com.vermouthx.stocker.components;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class StockerDefaultTableCellRender extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setHorizontalAlignment(SwingConstants.CENTER);
        // Always pass false for hasFocus to prevent cell focus border
        return super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
    }
}
