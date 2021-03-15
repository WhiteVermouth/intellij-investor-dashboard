package com.vermouthx.stocker.views;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.vermouthx.stocker.entities.StockerQuote;
import com.vermouthx.stocker.enums.StockerMarketType;
import com.vermouthx.stocker.settings.StockerSetting;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StockerStockDeleteDialog extends DialogWrapper {
    private final JScrollPane tbPane = new JBScrollPane();
    private final JTable mTable = new JBTable();
    private final String[] tableColumnIdentifiers = new String[]{"Select", "Symbol", "Name"};
    private final DefaultTableModel tbModel = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 0;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? Boolean.class : super.getColumnClass(columnIndex);
        }
    };
    private final DefaultTableCellRenderer tableCellRenderer = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    };

    public StockerStockDeleteDialog(Project project) {
        super(project, true);
        init();
        setTitle("Delete Stock Symbols");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        tbModel.setColumnIdentifiers(tableColumnIdentifiers);
        mTable.setModel(tbModel);
        mTable.getTableHeader().setDefaultRenderer(tableCellRenderer);
        mTable.getTableHeader().setReorderingAllowed(false);
        mTable.getColumn(tableColumnIdentifiers[0]).setPreferredWidth(20);
        for (int i = 1; i < tableColumnIdentifiers.length; i++) {
            mTable.getColumn(tableColumnIdentifiers[i]).setCellRenderer(tableCellRenderer);
        }
        mTable.setShowVerticalLines(false);
        tbPane.add(mTable);
        tbPane.setViewportView(mTable);
        tbPane.setPreferredSize(new Dimension(250, 300));
        return tbPane;
    }

    public void setupStockSymbols(List<StockerQuote> symbols) {
        for (StockerQuote symbol : symbols) {
            tbModel.addRow(new Object[]{false, symbol.getCode(), symbol.getName()});
        }
    }

    public List<String> deleteSymbols() {
        List<String> deletedSymbols = new ArrayList<>();
        StockerSetting setting = StockerSetting.Companion.getInstance();
        for (int row = 0; row < tbModel.getRowCount(); row++) {
            if ((boolean) tbModel.getValueAt(row, 0)) {
                setting.removeCode(StockerMarketType.AShare, (String) tbModel.getValueAt(row, 1));
                deletedSymbols.add((String) tbModel.getValueAt(row, 1));
            }
        }
        return deletedSymbols;
    }
}
