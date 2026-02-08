package com.vermouthx.stocker.views;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.vermouthx.stocker.components.StockerDefaultTableCellRender;
import com.vermouthx.stocker.components.StockerTableHeaderRender;
import com.vermouthx.stocker.components.StockerTableModel;
import com.vermouthx.stocker.entities.StockerQuote;
import com.vermouthx.stocker.entities.StockerSuggestion;
import com.vermouthx.stocker.enums.StockerMarketType;
import com.vermouthx.stocker.enums.StockerSortState;
import com.vermouthx.stocker.enums.StockerTableColumn;
import com.vermouthx.stocker.settings.StockerSetting;
import com.vermouthx.stocker.utils.StockerActionUtil;
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

public class StockerTableView implements Disposable {

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
    private final StockerDefaultTableCellRender codeRenderer = new CodeCellRenderer();
    private final StockerDefaultTableCellRender numericRenderer = new NumericCellRenderer();
    private final StockerDefaultTableCellRender changeRenderer = new ChangeCellRenderer();
    private final StockerDefaultTableCellRender percentRenderer = new PercentCellRenderer();
    private final StockerDefaultTableCellRender costRenderer = new CostCellRenderer();

    // Sorting state
    private StockerTableHeaderRender headerRenderer;
    private int lastSortColumn = -1;
    private StockerSortState currentSortState = StockerSortState.NONE;
    // Backup data only when sorting is active (cleared when returning to NONE state)
    private List<Object[]> sortBackupData = null;

    private volatile boolean disposed = false;

    public StockerTableView() {
        tableViews.add(this);
        syncColorPatternSetting();
        initPane();
        initTable();
    }

    /**
     * Clean up resources and remove this instance from the registry.
     * Called automatically when the parent Disposable is disposed.
     */
    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        tableViews.remove(this);

        // Clear data structures to help with garbage collection
        indices.clear();
        if (sortBackupData != null) {
            sortBackupData.clear();
            sortBackupData = null;
        }

        // Clear table model
        if (tbModel != null) {
            tbModel.setRowCount(0);
        }
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
        tbPane.setViewportBorder(BorderFactory.createEmptyBorder());

        JPanel iPane = new JPanel(new GridLayout(1, 4, 8, 0)); // Add horizontal gap between components
        iPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, JBColor.border()),
            BorderFactory.createEmptyBorder(8, 12, 8, 12) // Add padding to index panel
        ));

        // Style the index components
        Font indexFont = lbIndexValue.getFont().deriveFont(Font.BOLD, lbIndexValue.getFont().getSize() + 1f);
        lbIndexValue.setFont(indexFont);
        lbIndexExtent.setFont(indexFont);
        lbIndexPercent.setFont(indexFont);

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
    private static final String openingColumn = StockerTableColumn.OPENING.getTitle();
    private static final String closeColumn = StockerTableColumn.CLOSE.getTitle();
    private static final String lowColumn = StockerTableColumn.LOW.getTitle();
    private static final String highColumn = StockerTableColumn.HIGH.getTitle();
    private static final String changeColumn = StockerTableColumn.CHANGE.getTitle();
    private static final String percentColumn = StockerTableColumn.CHANGE_PERCENT.getTitle();
    private static final String costPriceColumn = StockerTableColumn.COST_PRICE.getTitle();
    private static final String holdingsColumn = StockerTableColumn.HOLDINGS.getTitle();
    private static final List<String> allColumns = StockerTableColumn.defaultTitles();

    private void initTable() {
        tbModel = new StockerTableModel();
        tbBody = new JBTable();
        JPopupMenu rowPopupMenu = createRowPopupMenu();

        tbBody.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                tbBody.clearSelection();
            }
        });
        tbBody.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleTableMouseEvent(e, rowPopupMenu);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleTableMouseEvent(e, rowPopupMenu);
            }
        });

        tbModel.setColumnIdentifiers(new String[]{codeColumn, nameColumn, currentColumn, openingColumn, closeColumn, lowColumn, highColumn, changeColumn, percentColumn, costPriceColumn, holdingsColumn});

        tbBody.setModel(tbModel);
        tbBody.setAutoCreateColumnsFromModel(false);

        // Table grid styling
        tbBody.setRowHeight(26);
        tbBody.setIntercellSpacing(new Dimension(0, 1));
        tbBody.setShowGrid(true);
        tbBody.setShowVerticalLines(false);
        tbBody.setShowHorizontalLines(true);
        tbBody.setGridColor(JBColor.namedColor("Table.gridColor", JBColor.border()));
        tbBody.setFillsViewportHeight(true);
        tbBody.getColumnModel().setColumnMargin(0);

        // Use IDE theme colors for selection
        tbBody.setSelectionBackground(JBColor.namedColor("Table.selectionBackground", UIManager.getColor("Table.selectionBackground")));
        tbBody.setSelectionForeground(JBColor.namedColor("Table.selectionForeground", UIManager.getColor("Table.selectionForeground")));

        // Avoid extra separator lines from custom LAF header UI; renderer will own divider painting.
        tbBody.getTableHeader().setUI(new javax.swing.plaf.basic.BasicTableHeaderUI());
        tbBody.getTableHeader().setReorderingAllowed(false);
        tbBody.getTableHeader().setPreferredSize(new Dimension(tbBody.getTableHeader().getWidth(), 30)); // Compact header to match rows
        tbBody.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
        headerRenderer = new StockerTableHeaderRender();
        tbBody.getTableHeader().setDefaultRenderer(headerRenderer);

        // Add header click listener for sorting with visual feedback
        tbBody.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = tbBody.getTableHeader().columnAtPoint(e.getPoint());
                if (column != -1) {
                    sortByColumn(column);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                tbBody.getTableHeader().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                tbBody.getTableHeader().setCursor(Cursor.getDefaultCursor());
            }
        });

        applyColumnVisibility();
        tbPane.setViewportView(tbBody);
    }

    private void handleTableMouseEvent(MouseEvent event, JPopupMenu rowPopupMenu) {
        int row = tbBody.rowAtPoint(event.getPoint());
        if (tbBody.isFocusOwner() && row == -1 && !event.isPopupTrigger()) {
            tbBody.clearSelection();
            return;
        }

        if (row != -1 && (event.isPopupTrigger() || SwingUtilities.isRightMouseButton(event))) {
            tbBody.setRowSelectionInterval(row, row);
        }

        if (event.isPopupTrigger() && row != -1) {
            rowPopupMenu.show(tbBody, event.getX(), event.getY());
        }
    }

    private JPopupMenu createRowPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteMenuItem = new JMenuItem("Delete");
        deleteMenuItem.setOpaque(true);
        deleteMenuItem.setRolloverEnabled(true);
        deleteMenuItem.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        Color defaultBackground = JBColor.namedColor("MenuItem.background", UIManager.getColor("MenuItem.background"));
        Color defaultForeground = JBColor.namedColor("MenuItem.foreground", UIManager.getColor("MenuItem.foreground"));
        Color hoverBackground = JBColor.namedColor(
                "MenuItem.selectionBackground",
                JBColor.namedColor("List.selectionBackground", tbBody.getSelectionBackground())
        );
        Color hoverForeground = JBColor.namedColor(
                "MenuItem.selectionForeground",
                JBColor.namedColor("List.selectionForeground", tbBody.getSelectionForeground())
        );
        deleteMenuItem.setBackground(defaultBackground);
        deleteMenuItem.setForeground(defaultForeground);
        deleteMenuItem.getModel().addChangeListener(e -> {
            ButtonModel model = deleteMenuItem.getModel();
            boolean hovering = model.isArmed() || model.isRollover();
            deleteMenuItem.setBackground(hovering ? hoverBackground : defaultBackground);
            deleteMenuItem.setForeground(hovering ? hoverForeground : defaultForeground);
        });
        deleteMenuItem.addActionListener(e -> deleteSelectedStock());
        popupMenu.add(deleteMenuItem);
        return popupMenu;
    }

    private void deleteSelectedStock() {
        int selectedRow = tbBody.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        Object codeValue = tbModel.getValueAt(selectedRow, 0);
        if (codeValue == null) {
            return;
        }
        String code = codeValue.toString();
        StockerSetting setting = StockerSetting.Companion.getInstance();
        StockerMarketType market = setting.marketOf(code);
        if (market == null) {
            return;
        }

        Object nameValue = tbModel.getValueAt(selectedRow, 1);
        String name = nameValue == null ? code : nameValue.toString();
        StockerActionUtil.removeStock(market, new StockerSuggestion(code, name, market));
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

        // Re-apply after column model rebuild to keep header/body cell geometry in sync.
        tbBody.getColumnModel().setColumnMargin(0);
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

    public void refreshColorPattern() {
        syncColorPatternSetting();
        updateIndex();
        tbBody.revalidate();
        tbBody.repaint();
    }

    public static void refreshAllColorPatterns() {
        SwingUtilities.invokeLater(() -> {
            synchronized (tableViews) {
                for (StockerTableView view : tableViews) {
                    view.refreshColorPattern();
                }
            }
        });
    }

    private void applyColumnRenderers() {
        TableColumn code = getColumnIfPresent(codeColumn);
        if (code != null) {
            code.setCellRenderer(codeRenderer);
        }
        TableColumn name = getColumnIfPresent(nameColumn);
        if (name != null) {
            name.setCellRenderer(defaultRenderer);
        }
        TableColumn current = getColumnIfPresent(currentColumn);
        if (current != null) {
            current.setCellRenderer(numericRenderer);
        }
        TableColumn opening = getColumnIfPresent(openingColumn);
        if (opening != null) {
            opening.setCellRenderer(numericRenderer);
        }
        TableColumn close = getColumnIfPresent(closeColumn);
        if (close != null) {
            close.setCellRenderer(numericRenderer);
        }
        TableColumn low = getColumnIfPresent(lowColumn);
        if (low != null) {
            low.setCellRenderer(numericRenderer);
        }
        TableColumn high = getColumnIfPresent(highColumn);
        if (high != null) {
            high.setCellRenderer(numericRenderer);
        }
        TableColumn change = getColumnIfPresent(changeColumn);
        if (change != null) {
            change.setCellRenderer(changeRenderer);
        }
        TableColumn percent = getColumnIfPresent(percentColumn);
        if (percent != null) {
            percent.setCellRenderer(percentRenderer);
        }
        TableColumn costPrice = getColumnIfPresent(costPriceColumn);
        if (costPrice != null) {
            costPrice.setCellRenderer(costRenderer);
        }
        TableColumn holdings = getColumnIfPresent(holdingsColumn);
        if (holdings != null) {
            holdings.setCellRenderer(numericRenderer);
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
        currentSortState = StockerSortState.NONE;
        lastSortColumn = -1;
        // Clear backup data when sort is cleared externally
        if (sortBackupData != null) {
            sortBackupData.clear();
            sortBackupData = null;
        }
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

        // Sort the table data using optimized in-place sorting
        sortTableDataOptimized(columnName, currentSortState);
    }

    /**
     * Optimized sorting that works with row indices instead of copying entire dataset.
     * Backup data is only stored when sorting is active and cleared when returning to NONE state.
     */
    private void sortTableDataOptimized(String columnName, StockerSortState sortState) {
        int rowCount = tbModel.getRowCount();
        if (rowCount == 0) {
            return;
        }

        // For NONE state, restore original data and clear backup
        if (sortState == StockerSortState.NONE) {
            if (sortBackupData != null && !sortBackupData.isEmpty()) {
                tbModel.setRowCount(0);
                for (Object[] row : sortBackupData) {
                    tbModel.addRow(row);
                }
                sortBackupData.clear();
                sortBackupData = null;
            }
            return;
        }

        // Capture original data before first sort (only once)
        if (sortBackupData == null) {
            sortBackupData = new ArrayList<>(rowCount);
            for (int i = 0; i < rowCount; i++) {
                Object[] row = new Object[tbModel.getColumnCount()];
                for (int j = 0; j < tbModel.getColumnCount(); j++) {
                    row[j] = tbModel.getValueAt(i, j);
                }
                sortBackupData.add(row);
            }
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
        final boolean ascending = (sortState == StockerSortState.ASCENDING);

        // Create lightweight index array instead of copying all data
        Integer[] indices = new Integer[rowCount];
        for (int i = 0; i < rowCount; i++) {
            indices[i] = i;
        }

        // Sort indices based on values - only references are sorted, not actual data
        java.util.Arrays.sort(indices, (i1, i2) -> {
            Object val1 = tbModel.getValueAt(i1, sortColumnIndex);
            Object val2 = tbModel.getValueAt(i2, sortColumnIndex);

            int result = 0;

            if (columnName.equals(codeColumn) || columnName.equals(nameColumn)) {
                // Alphabetical sorting
                String str1 = val1 != null ? val1.toString() : "";
                String str2 = val2 != null ? val2.toString() : "";
                result = str1.compareToIgnoreCase(str2);
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
            } else {
                // Numeric sorting for all other columns (Current, Opening, Close, Low, High, Change)
                Double num1 = parseDouble(val1);
                Double num2 = parseDouble(val2);
                if (num1 != null && num2 != null) {
                    result = Double.compare(num1, num2);
                } else if (num1 != null) {
                    result = 1;
                } else if (num2 != null) {
                    result = -1;
                }
            }

            return ascending ? result : -result;
        });

        // Reorder rows based on sorted indices - minimal memory footprint
        java.util.List<Object[]> sortedRows = new ArrayList<>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            int sourceIndex = indices[i];
            Object[] row = new Object[tbModel.getColumnCount()];
            for (int j = 0; j < tbModel.getColumnCount(); j++) {
                row[j] = tbModel.getValueAt(sourceIndex, j);
            }
            sortedRows.add(row);
        }

        // Update table with sorted data
        tbModel.setRowCount(0);
        for (Object[] row : sortedRows) {
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

    // Inner class for Code column renderer that strips BTC prefix from crypto codes
    private class CodeCellRenderer extends StockerDefaultTableCellRender {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setHorizontalAlignment(DefaultTableCellRenderer.CENTER);

            // Strip BTC prefix from crypto codes for display
            if (value != null) {
                String code = value.toString();
                if (code.startsWith("BTC") && code.length() > 3) {
                    // Remove the "BTC" prefix for display
                    value = code.substring(3);
                }
            }

            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    // Inner class for numeric columns (Current, Opening, Close, Low, High) with color coding based on percentage
    private class NumericCellRenderer extends StockerDefaultTableCellRender {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
            if (isSelected) {
                return component;
            }
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
                        } else {
                            setForeground(table.getForeground());
                        }
                    } else {
                        setForeground(table.getForeground());
                    }
                } else {
                    setForeground(table.getForeground());
                }
            } catch (IllegalArgumentException e) {
                setForeground(table.getForeground());
            }
            return component;
        }
    }

    // Inner class for Change column renderer with color coding based on value sign
    private class ChangeCellRenderer extends StockerDefaultTableCellRender {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
            if (isSelected) {
                return component;
            }
            if (value != null && !value.toString().isEmpty()) {
                try {
                    Double changeValue = parseDouble(value);
                    if (changeValue != null) {
                        if (changeValue > 0) {
                            setForeground(upColor);
                        } else if (changeValue < 0) {
                            setForeground(downColor);
                        } else {
                            setForeground(zeroColor);
                        }
                    } else {
                        setForeground(table.getForeground());
                    }
                } catch (Exception e) {
                    setForeground(table.getForeground());
                }
            } else {
                setForeground(table.getForeground());
            }
            return component;
        }
    }

    // Inner class for Change% column renderer with color coding
    private class PercentCellRenderer extends StockerDefaultTableCellRender {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
            if (isSelected) {
                return component;
            }
            if (value == null) {
                setForeground(table.getForeground());
                return component;
            }
            try {
                String percentValue = value.toString();
                Double v = parsePercentage(percentValue);
                if (v != null) {
                    applyColorPatternToTable(v, this);
                } else {
                    setForeground(table.getForeground());
                }
            } catch (NumberFormatException e) {
                setForeground(table.getForeground());
            }
            return component;
        }
    }

    // Inner class for Cost column renderer with inverted color coding (cost > current = up color, cost < current = down color)
    private class CostCellRenderer extends StockerDefaultTableCellRender {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
            if (isSelected) {
                return component;
            }
            if (value == null || value.toString().isEmpty()) {
                setForeground(table.getForeground());
                return component;
            }
            try {
                // Get the cost price value
                Double costPrice = parseDouble(value);
                if (costPrice == null) {
                    setForeground(table.getForeground());
                    return component;
                }

                // Get the current price from the same row
                int currentModelIndex = -1;
                if (table.getModel() instanceof DefaultTableModel) {
                    currentModelIndex = ((DefaultTableModel) table.getModel()).findColumn(currentColumn);
                }
                if (currentModelIndex != -1 && row >= 0 && row < table.getModel().getRowCount()) {
                    Object currentValue = table.getModel().getValueAt(row, currentModelIndex);
                    if (currentValue != null) {
                        Double currentPrice = parseDouble(currentValue);
                        if (currentPrice != null) {
                            // If cost > current, show down color (we're losing money)
                            // If cost < current, show up color (we're making money)
                            // If cost == current, show zero color
                            if (costPrice > currentPrice) {
                                setForeground(downColor);
                            } else if (costPrice < currentPrice) {
                                setForeground(upColor);
                            } else {
                                setForeground(zeroColor);
                            }
                        } else {
                            setForeground(table.getForeground());
                        }
                    } else {
                        setForeground(table.getForeground());
                    }
                } else {
                    setForeground(table.getForeground());
                }
            } catch (Exception e) {
                setForeground(table.getForeground());
            }
            return component;
        }
    }

}
