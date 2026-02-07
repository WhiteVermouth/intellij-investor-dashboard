package com.vermouthx.stocker.components;

import com.intellij.ui.JBColor;
import com.vermouthx.stocker.enums.StockerSortState;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class StockerTableHeaderRender extends DefaultTableCellRenderer implements TableCellRenderer {

    private int sortColumn = -1;
    private StockerSortState sortState = StockerSortState.NONE;

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
        super.getTableCellRendererComponent(table, value, false, false, row, column);

        setHorizontalAlignment(SwingConstants.CENTER);
        setOpaque(true);
        setBackground(JBColor.namedColor("TableHeader.background", UIManager.getColor("TableHeader.background")));

        Border innerPadding = BorderFactory.createEmptyBorder(2, 8, 2, 8);
        boolean isLastVisibleColumn = column == table.getColumnCount() - 1;
        Border dividerBorder = isLastVisibleColumn
            ? innerPadding
            : BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, table.getGridColor()),
                innerPadding
            );
        setBorder(dividerBorder);

        Font currentFont = getFont();
        setFont(currentFont.deriveFont(Font.BOLD));

        if (column == sortColumn && sortState != StockerSortState.NONE) {
            String sortIndicator = sortState == StockerSortState.ASCENDING ? " ↑" : " ↓";
            setText((value != null ? value.toString() : "") + sortIndicator);
            setForeground(JBColor.namedColor("Label.selectedForeground", getForeground()));
        } else {
            setText(value != null ? value.toString() : "");
            setForeground(JBColor.namedColor("TableHeader.foreground", UIManager.getColor("TableHeader.foreground")));
        }
        return this;
    }
}
