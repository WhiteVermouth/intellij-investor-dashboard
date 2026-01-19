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

    public StockerTableView() {
        tableViews.add(this);
        syncColorPatternSetting();
        initPane();
        initTable();
    }

    public void syncIndices(List<StockerQuote> indices) {
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
            } else {
                cbIndex.setSelectedIndex(0);
            }
        }
        syncColorPatternSetting();
        updateIndex();
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
        tbBody.getTableHeader().setDefaultRenderer(new StockerTableHeaderRender(tbBody));

        applyColumnVisibility();
        tbPane.setViewportView(tbBody);
    }

    private void applyColumnVisibility() {
        StockerSetting setting = StockerSetting.Companion.getInstance();
        List<String> visibleColumns = setting.getVisibleTableColumns();

        tbBody.createDefaultColumnsFromModel();
        tbBody.getTableHeader().setReorderingAllowed(false);
        tbBody.getTableHeader().setDefaultRenderer(new StockerTableHeaderRender(tbBody));

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
            code.setCellRenderer(new StockerDefaultTableCellRender());
        }
        TableColumn name = getColumnIfPresent(nameColumn);
        if (name != null) {
            name.setCellRenderer(new StockerDefaultTableCellRender());
        }
        TableColumn current = getColumnIfPresent(currentColumn);
        if (current != null) {
            current.setCellRenderer(new StockerDefaultTableCellRender() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
                    try {
                        int percentModelIndex = -1;
                        if (table.getModel() instanceof DefaultTableModel) {
                            percentModelIndex = ((DefaultTableModel) table.getModel()).findColumn(percentColumn);
                        }
                        if (percentModelIndex != -1) {
                            Object percentValue = table.getModel().getValueAt(row, percentModelIndex);
                            if (percentValue != null) {
                                Double v = parsePercentage(percentValue.toString());
                                if (v != null) {
                                    applyColorPatternToTable(v, this);
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Fallback to default foreground color on parsing error
                        setForeground(JBColor.foreground());
                    }
                    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                }
            });
        }
        TableColumn percent = getColumnIfPresent(percentColumn);
        if (percent != null) {
            percent.setCellRenderer(new StockerDefaultTableCellRender() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
                    try {
                        String percentValue = value.toString();
                        Double v = parsePercentage(percentValue);
                        if (v != null) {
                            applyColorPatternToTable(v, this);
                        }
                    } catch (Exception e) {
                        // Fallback to default foreground color on parsing error
                        setForeground(JBColor.foreground());
                    }
                    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                }
            });
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

}
