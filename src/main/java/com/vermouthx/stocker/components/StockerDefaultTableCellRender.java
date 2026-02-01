package com.vermouthx.stocker.components;

import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class StockerDefaultTableCellRender extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setHorizontalAlignment(SwingConstants.CENTER);
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        // Add padding to cells for better readability - reduced vertical padding
        setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        
        // Use uniform background without alternating colors to avoid shadowed effect
        if (!isSelected) {
            setBackground(JBColor.background());
        }
        
        return component;
    }
}
