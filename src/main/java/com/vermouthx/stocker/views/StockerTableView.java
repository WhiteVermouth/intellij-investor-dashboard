package com.vermouthx.stocker.views;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.vermouthx.stocker.components.StockerDefaultTableCellRender;
import com.vermouthx.stocker.components.StockerTableHeaderRender;
import com.vermouthx.stocker.components.StockerTableModel;
import com.vermouthx.stocker.entities.StockerQuote;
import com.vermouthx.stocker.enums.StockerSortState;
import com.vermouthx.stocker.enums.StockerTableColumn;
import com.vermouthx.stocker.settings.StockerSetting;
import com.vermouthx.stocker.utils.StockerPinyinUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class StockerTableView {

    private static final List<StockerTableView> tableViews = Collections.synchronizedList(new ArrayList<>());

    private JPanel mPane;
    private JScrollPane tbPane;
    private Color upColor;
    private Color downColor;
    private Color zeroColor;
    private JBTable tbBody;
    private StockerTableModel tbModel;

    private final ComboBox<String> cbIndex = new ComboBox<>();
    private final JBLabel lbIndexValue = new JBLabel("", SwingConstants.CENTER);
    private final JBLabel lbIndexExtent = new JBLabel("", SwingConstants.CENTER);
    private final JBLabel lbIndexPercent = new JBLabel("", SwingConstants.CENTER);
    private List<StockerQuote> indices = new ArrayList<>();

    // Cache renderers to avoid creating new instances on every refresh
    private final StockerDefaultTableCellRender defaultRenderer = new StockerDefaultTableCellRender();
    private final StockerDefaultTableCellRender currentRenderer = new CurrentCellRenderer();
    private final StockerDefaultTableCellRender percentRenderer = new PercentCellRenderer();
    
    // Sorting state
    private StockerTableHeaderRender headerRenderer;
    private int lastSortColumn = -1;
    private StockerSortState currentSortState = StockerSortState.NONE;
    private List<Object[]> originalTableData = new ArrayList<>();

    public StockerTableView() {
        tableViews.add(this);
        syncColorPatternSetting();
        initPane();
        initTable();
    }

    /**
     * Clean up resources and remove this instance from the registry.
     * Should be called when the tool window is closed or the project is disposed.
     * Note: Currently not called automatically - consider implementing Disposable in parent components.
     */
    public void dispose() {
        tableViews.remove(this);
    }

    public void syncIndices(List<StockerQuote> indices) {
        SwingUtilities.invokeLater(() -> {
            this.indices = indices;
            StockerSetting setting = StockerSetting.Companion.getInstance();

            boolean shouldRefresh = cbIndex.getItemCount() != indices.size();
            if (!shouldRefresh) {
                for (int i = 0; i < indices.size(); i++) {
                    StockerQuote index = indices.get(i);
                    String displayName = setting.getDisplayName(index.getCode(), index.getName());
                    if (!Objects.equals(displayName, cbIndex.getItemAt(i))) {
                        shouldRefresh = true;
                        break;
                    }
                }
            }

            if (shouldRefresh && !indices.isEmpty()) {
                String selectedDisplayName = cbIndex.getSelectedItem() == null ? null : cbIndex.getSelectedItem().toString();
                String selectedCode = findIndexCodeByDisplayName(selectedDisplayName, setting);
                cbIndex.removeAllItems();
                indices.forEach(i -> {
                    String displayName = setting.getDisplayName(i.getCode(), i.getName());
                    cbIndex.addItem(displayName);
                });
                if (selectedCode != null) {
                    for (int i = 0; i < indices.size(); i++) {
                        if (indices.get(i).getCode().equals(selectedCode)) {
                            cbIndex.setSelectedIndex(i);
                            break;
                        }
                    }
                } else if (!indices.isEmpty()) {
                    cbIndex.setSelectedIndex(0);
                }
            }
            syncColorPatternSetting();
            updateIndex();
        });
    }

    private void syncColorPatternSetting() {
        StockerSetting setting = StockerSetting.Companion.getInstance();
        switch (setting.getQuoteColorPattern()) {
            case RED_UP_GREEN_DOWN:
                upColor = JBColor.RED;
                downColor = JBColor.GREEN;
                zeroColor = JBColor.GRAY;
                break;
            case GREEN_UP_RED_DOWN:
                upColor = JBColor.GREEN;
                downColor = JBColor.RED;
                zeroColor = JBColor.GRAY;
                break;
            default:
                upColor = JBColor.foreground();
                downColor = JBColor.foreground();
                zeroColor = JBColor.foreground();
                break;
        }
    }

    private void updateIndex() {
        if (cbIndex.getSelectedIndex() != -1 && cbIndex.getSelectedItem() != null) {
            String selectedDisplayName = cbIndex.getSelectedItem().toString();
            StockerSetting setting = StockerSetting.Companion.getInstance();
            String selectedCode = findIndexCodeByDisplayName(selectedDisplayName, setting);

            for (StockerQuote index : indices) {
                String displayName = setting.getDisplayName(index.getCode(), index.getName());
                boolean isSelected = selectedCode != null ? index.getCode().equals(selectedCode) : displayName.equals(selectedDisplayName);
                if (isSelected) {
                    lbIndexValue.setText(Double.toString(index.getCurrent()));
                    lbIndexExtent.setText(Double.toString(index.getChange()));
                    lbIndexPercent.setText(index.getPercentage() + "%");
                    double value = index.getPercentage();
                    if (value > 0) {
                        lbIndexValue.setForeground(upColor);
                        lbIndexExtent.setForeground(upColor);
                        lbIndexPercent.setForeground(upColor);
                    } else if (value < 0) {
                        lbIndexValue.setForeground(downColor);
                        lbIndexExtent.setForeground(downColor);
                        lbIndexPercent.setForeground(downColor);
                    } else {
                        lbIndexValue.setForeground(zeroColor);
                        lbIndexExtent.setForeground(zeroColor);
                        lbIndexPercent.setForeground(zeroColor);
                    }
                    break;
                }
            }
        }
    }

    private String findIndexCodeByDisplayName(String displayName, StockerSetting setting) {
        if (displayName == null || displayName.isEmpty()) {
            return null;
        }
        for (StockerQuote index : indices) {
            String code = index.getCode();
            String customName = setting.getCustomName(code);
            if (customName != null && customName.equals(displayName)) {
                return code;
            }
            String originalName = index.getName();
            if (displayName.equals(originalName)) {
                return code;
            }
            if (displayName.equals(StockerPinyinUtil.INSTANCE.toPinyin(originalName))) {
                return code;
            }
        }
        return null;
    }

    private void initPane() {
        tbPane = new JBScrollPane();
        tbPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel iPane = new JPanel(new GridLayout(1, 4));
        iPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, JBColor.border()));
        iPane.add(cbIndex);
        iPane.add(lbIndexValue);
        iPane.add(lbIndexExtent);
        iPane.add(lbIndexPercent);
        cbIndex.addItemListener(i -> updateIndex());
        mPane = new JPanel(new BorderLayout());
        mPane.add(tbPane, BorderLayout.CENTER);
        mPane.add(iPane, BorderLayout.SOUTH);
    }

    private static final String codeColumn = StockerTableColumn.SYMBOL.getTitle();
    private static final String nameColumn = StockerTableColumn.NAME.getTitle();
    private static final String currentColumn = StockerTableColumn.CURRENT.getTitle();
    private static final String percentColumn = StockerTableColumn.CHANGE_PERCENT.getTitle();
    private static final List<String> allColumns = StockerTableColumn.defaultTitles();

    private void initTable() {
        tbModel = new StockerTableModel();
        tbBody = new JBTable();

        tbBody.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                tbBody.clearSelection();
            }
        });
        tbBody.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (tbBody.isFocusOwner() && tbBody.rowAtPoint(e.getPoint()) == -1) {
                    tbBody.clearSelection();
                }
            }
        });

        tbModel.setColumnIdentifiers(new String[]{codeColumn, nameColumn, currentColumn, percentColumn});

        tbBody.setShowVerticalLines(false);
        tbBody.setModel(tbModel);
        tbBody.setAutoCreateColumnsFromModel(false);

        tbBody.getTableHeader().setReorderingAllowed(false);
        headerRenderer = new StockerTableHeaderRender(tbBody);
        tbBody.getTableHeader().setDefaultRenderer(headerRenderer);
        
        // Add header click listener for sorting
        tbBody.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = tbBody.getTableHeader().columnAtPoint(e.getPoint());
                if (column != -1) {
                    sortByColumn(column);
                }
            }
        });

        applyColumnVisibility();
        tbPane.setViewportView(tbBody);
    }

    private void applyColumnVisibility() {
        StockerSetting setting = StockerSetting.Companion.getInstance();
        List<String> visibleColumns = setting.getVisibleTableColumns();

        tbBody.createDefaultColumnsFromModel();

        for (String column : allColumns) {
            if (!visibleColumns.contains(column)) {
                TableColumn tableColumn = getColumnIfPresent(column);
                if (tableColumn != null) {
                    tbBody.removeColumn(tableColumn);
                }
            }
        }

        applyColumnRenderers();
    }

    public void refreshColumnVisibility() {
        applyColumnVisibility();
        tbBody.revalidate();
        tbBody.repaint();
    }

    public static void refreshAllColumnVisibility() {
        SwingUtilities.invokeLater(() -> {
            synchronized (tableViews) {
                for (StockerTableView view : tableViews) {
                    view.refreshColumnVisibility();
                }
            }
        });
    }

    private void applyColumnRenderers() {
        TableColumn code = getColumnIfPresent(codeColumn);
        if (code != null) {
            code.setCellRenderer(defaultRenderer);
        }
        TableColumn name = getColumnIfPresent(nameColumn);
        if (name != null) {
            name.setCellRenderer(defaultRenderer);
        }
        TableColumn current = getColumnIfPresent(currentColumn);
        if (current != null) {
            current.setCellRenderer(currentRenderer);
        }
        TableColumn percent = getColumnIfPresent(percentColumn);
        if (percent != null) {
            percent.setCellRenderer(percentRenderer);
        }
    }

    private TableColumn getColumnIfPresent(String columnName) {
        try {
            return tbBody.getColumn(columnName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void applyColorPatternToTable(Double value, DefaultTableCellRenderer renderer) {
        if (value > 0) {
            renderer.setForeground(upColor);
        } else if (value < 0) {
            renderer.setForeground(downColor);
        } else {
            renderer.setForeground(zeroColor);
        }
    }

    private Double parsePercentage(String percentStr) {
        if (percentStr == null || percentStr.isEmpty()) {
            return null;
        }
        try {
            int percentIndex = percentStr.indexOf("%");
            if (percentIndex > 0) {
                return Double.parseDouble(percentStr.substring(0, percentIndex));
            }
            return Double.parseDouble(percentStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public JComponent getComponent() {
        return mPane;
    }

    public JBTable getTableBody() {
        return tbBody;
    }

    public DefaultTableModel getTableModel() {
        return tbModel;
    }

    /**
     * Clears the sort state and resets to unsorted view.
     * Should be called when table data is externally modified.
     */
    public void clearSortState() {
        originalTableData.clear();
        currentSortState = StockerSortState.NONE;
        lastSortColumn = -1;
        if (headerRenderer != null) {
            headerRenderer.setSortState(-1, StockerSortState.NONE);
            if (tbBody != null && tbBody.getTableHeader() != null) {
                tbBody.getTableHeader().repaint();
            }
        }
    }

    private void sortByColumn(int column) {
        String columnName = tbBody.getColumnName(column);
        
        // Cycle through sort states: NONE -> ASCENDING -> DESCENDING -> NONE
        if (column == lastSortColumn) {
            // Same column clicked, cycle to next state
            switch (currentSortState) {
                case NONE:
                    currentSortState = StockerSortState.ASCENDING;
                    break;
                case ASCENDING:
                    currentSortState = StockerSortState.DESCENDING;
                    break;
                case DESCENDING:
                    currentSortState = StockerSortState.NONE;
                    break;
            }
        } else {
            // Different column clicked, start with ASCENDING
            lastSortColumn = column;
            currentSortState = StockerSortState.ASCENDING;
        }
        
        // Update header renderer
        headerRenderer.setSortState(column, currentSortState);
        tbBody.getTableHeader().repaint();
        
        // Sort the table data or restore original
        if (currentSortState == StockerSortState.NONE) {
            restoreOriginalTableData();
        } else {
            sortTableData(columnName, currentSortState == StockerSortState.ASCENDING);
        }
    }
    
    private void captureOriginalTableData() {
        originalTableData.clear();
        int rowCount = tbModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            Object[] row = new Object[tbModel.getColumnCount()];
            for (int j = 0; j < tbModel.getColumnCount(); j++) {
                row[j] = tbModel.getValueAt(i, j);
            }
            originalTableData.add(row);
        }
    }
    
    private void restoreOriginalTableData() {
        if (originalTableData.isEmpty()) {
            return;
        }
        
        tbModel.setRowCount(0);
        for (Object[] row : originalTableData) {
            tbModel.addRow(row);
        }
    }
    
    private void sortTableData(String columnName, boolean ascending) {
        int rowCount = tbModel.getRowCount();
        if (rowCount == 0) {
            return;
        }
        
        // Capture original data if not already captured or if data changed
        if (originalTableData.isEmpty() || originalTableData.size() != rowCount) {
            captureOriginalTableData();
        }
        
        // Convert table data to a list of rows
        java.util.List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            Object[] row = new Object[tbModel.getColumnCount()];
            for (int j = 0; j < tbModel.getColumnCount(); j++) {
                row[j] = tbModel.getValueAt(i, j);
            }
            rows.add(row);
        }
        
        // Get the column index in the model
        int columnIndex = -1;
        for (int i = 0; i < tbModel.getColumnCount(); i++) {
            if (tbModel.getColumnName(i).equals(columnName)) {
                columnIndex = i;
                break;
            }
        }
        
        if (columnIndex == -1) {
            return;
        }
        
        final int sortColumnIndex = columnIndex;
        
        // Sort based on column type
        rows.sort((row1, row2) -> {
            Object val1 = row1[sortColumnIndex];
            Object val2 = row2[sortColumnIndex];
            
            int result = 0;
            
            if (columnName.equals(codeColumn) || columnName.equals(nameColumn)) {
                // Alphabetical sorting
                String str1 = val1 != null ? val1.toString() : "";
                String str2 = val2 != null ? val2.toString() : "";
                result = str1.compareToIgnoreCase(str2);
            } else if (columnName.equals(currentColumn)) {
                // Numeric sorting for Current column
                Double num1 = parseDouble(val1);
                Double num2 = parseDouble(val2);
                if (num1 != null && num2 != null) {
                    result = Double.compare(num1, num2);
                } else if (num1 != null) {
                    result = 1;
                } else if (num2 != null) {
                    result = -1;
                }
            } else if (columnName.equals(percentColumn)) {
                // Numeric sorting for Change% column (parse percentage values)
                Double percent1 = parsePercentage(val1 != null ? val1.toString() : "");
                Double percent2 = parsePercentage(val2 != null ? val2.toString() : "");
                if (percent1 != null && percent2 != null) {
                    result = Double.compare(percent1, percent2);
                } else if (percent1 != null) {
                    result = 1;
                } else if (percent2 != null) {
                    result = -1;
                }
            }
            
            return ascending ? result : -result;
        });
        
        // Clear the table and repopulate with sorted data
        tbModel.setRowCount(0);
        for (Object[] row : rows) {
            tbModel.addRow(row);
        }
    }
    
    private Double parseDouble(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Inner class for Current column renderer with color coding
    private class CurrentCellRenderer extends StockerDefaultTableCellRender {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
            try {
                int percentModelIndex = -1;
                if (table.getModel() instanceof DefaultTableModel) {
                    percentModelIndex = ((DefaultTableModel) table.getModel()).findColumn(percentColumn);
                }
                if (percentModelIndex != -1 && row >= 0 && row < table.getModel().getRowCount()) {
                    Object percentValue = table.getModel().getValueAt(row, percentModelIndex);
                    if (percentValue != null) {
                        Double v = parsePercentage(percentValue.toString());
                        if (v != null) {
                            applyColorPatternToTable(v, this);
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                // Fallback to default foreground color on parsing error
                setForeground(JBColor.foreground());
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    // Inner class for Change% column renderer with color coding
    private class PercentCellRenderer extends StockerDefaultTableCellRender {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
            if (value == null) {
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
            try {
                String percentValue = value.toString();
                Double v = parsePercentage(percentValue);
                if (v != null) {
                    applyColorPatternToTable(v, this);
                }
            } catch (NumberFormatException e) {
                // Fallback to default foreground color on parsing error
                setForeground(JBColor.foreground());
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

}
