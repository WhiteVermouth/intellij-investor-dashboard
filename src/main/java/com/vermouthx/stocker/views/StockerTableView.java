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
import com.vermouthx.stocker.settings.StockerSetting;
import com.vermouthx.stocker.utils.StockerPinyinUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class StockerTableView {

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
                boolean isSelected = selectedCode != null
                        ? index.getCode().equals(selectedCode)
                        : displayName.equals(selectedDisplayName);
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
        
        // Clear table selection when clicking in scroll pane empty area
        tbPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                clearTableSelection();
            }
        });
        
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
        
        // Clear table selection when clicking anywhere in main panel
        mPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                clearTableSelection();
            }
        });
    }
    
    private void clearTableSelection() {
        if (tbBody != null) {
            tbBody.clearSelection();
        }
    }

    private static final String codeColumn = "Symbol";
    private static final String nameColumn = "Name";
    private static final String currentColumn = "Current";
    private static final String percentColumn = "Change%";

    private void initTable() {
        tbModel = new StockerTableModel();
        tbBody = new JBTable();
        
        // Configure table to only allow row selection, not cell selection
        tbBody.setRowSelectionAllowed(true);
        tbBody.setColumnSelectionAllowed(false);
        tbBody.setCellSelectionEnabled(false);
        
        // Disable cell focus border
        tbBody.setFocusable(false);
        
        tbBody.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int row = tbBody.rowAtPoint(e.getPoint());
                if (row >= 0 && row < tbBody.getRowCount()) {
                    if (tbBody.getSelectedRows().length == 0 || Arrays.stream(tbBody.getSelectedRows()).noneMatch(p -> p == row)) {
                        tbBody.setRowSelectionInterval(row, row);
                    }
                } else {
                    clearTableSelection();
                }
            }
        });
        tbModel.setColumnIdentifiers(new String[]{codeColumn, nameColumn, currentColumn, percentColumn});

        tbBody.setShowVerticalLines(false);
        tbBody.setModel(tbModel);

        tbBody.getTableHeader().setReorderingAllowed(false);
        tbBody.getTableHeader().setDefaultRenderer(new StockerTableHeaderRender(tbBody));

        tbBody.getColumn(codeColumn).setCellRenderer(new StockerDefaultTableCellRender());
        tbBody.getColumn(nameColumn).setCellRenderer(new StockerDefaultTableCellRender());
        tbBody.getColumn(currentColumn).setCellRenderer(new StockerDefaultTableCellRender() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
                try {
                    String percent = table.getValueAt(row, table.getColumn(percentColumn).getModelIndex()).toString();
                    Double v = parsePercentage(percent);
                    if (v != null) {
                        applyColorPatternToTable(v, this);
                    }
                } catch (Exception e) {
                    // Fallback to default foreground color on parsing error
                    setForeground(JBColor.foreground());
                }
                // Always pass false for hasFocus to prevent cell focus border
                return super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            }
        });
        tbBody.getColumn(percentColumn).setCellRenderer(new StockerDefaultTableCellRender() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
                try {
                    String percent = value.toString();
                    Double v = parsePercentage(percent);
                    if (v != null) {
                        applyColorPatternToTable(v, this);
                    }
                } catch (Exception e) {
                    // Fallback to default foreground color on parsing error
                    setForeground(JBColor.foreground());
                }
                // Always pass false for hasFocus to prevent cell focus border
                return super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            }
        });
        tbPane.setViewportView(tbBody);
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

    public void refreshDisplay() {
        syncColorPatternSetting();
        updateIndex();
        // Trigger table repaint to update cell colors
        tbBody.repaint();
    }

}
