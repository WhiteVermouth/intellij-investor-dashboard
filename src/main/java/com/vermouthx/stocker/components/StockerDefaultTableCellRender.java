package com.vermouthx.stocker.components;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class StockerDefaultTableCellRender extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        setHorizontalAlignment(SwingConstants.CENTER);
        Border innerPadding = BorderFactory.createEmptyBorder(2, 8, 2, 8);
        boolean isLastVisibleColumn = column == table.getColumnCount() - 1;
        Border dividerBorder = isLastVisibleColumn
            ? innerPadding
            : BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, table.getGridColor()),
                innerPadding
            );
        setBorder(dividerBorder);

        if (!isSelected) {
            setBackground(table.getBackground());
        }

        return component;
    }
}
