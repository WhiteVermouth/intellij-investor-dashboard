package com.vermouthx.stocker.views;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.vermouthx.stocker.entities.StockerQuote;
import com.vermouthx.stocker.enums.StockerQuoteColorPattern;
import com.vermouthx.stocker.settings.StockerSetting;

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
    private JTable tbBody;
    private DefaultTableModel tbModel;

    private final ComboBox<String> cbIndex = new ComboBox<>();
    private final JLabel lbIndexValue = new JBLabel("", SwingConstants.CENTER);
    private final JLabel lbIndexExtent = new JBLabel("", SwingConstants.CENTER);
    private final JLabel lbIndexPercent = new JBLabel("", SwingConstants.CENTER);
    private List<StockerQuote> indices = new ArrayList<>();

    public StockerTableView() {
        syncColorPatternSetting();
        initPane();
        initTable();
    }

    public void syncIndices(List<StockerQuote> indices) {
        this.indices = indices;
        if (cbIndex.getItemCount() == 0 && !indices.isEmpty()) {
            indices.forEach(i -> cbIndex.addItem(i.getName()));
            cbIndex.setSelectedIndex(0);
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
        if (cbIndex.getSelectedIndex() != -1) {
            String name = Objects.requireNonNull(cbIndex.getSelectedItem()).toString();
            for (StockerQuote index : indices) {
                if (index.getName().equals(name)) {
                    lbIndexValue.setText(index.getCurrent());
                    lbIndexExtent.setText(index.getChange());
                    lbIndexPercent.setText(index.getPercentage());
                    applyColorPatternToIndex(index.getPercentage());
                    break;
                }
            }
        }
    }

    private void applyColorPatternToIndex(String value) {
        if (value.startsWith("+")) {
            lbIndexValue.setForeground(upColor);
            lbIndexExtent.setForeground(upColor);
            lbIndexPercent.setForeground(upColor);
        } else if (value.startsWith("-")) {
            lbIndexValue.setForeground(downColor);
            lbIndexExtent.setForeground(downColor);
            lbIndexPercent.setForeground(downColor);
        } else {
            lbIndexValue.setForeground(zeroColor);
            lbIndexExtent.setForeground(zeroColor);
            lbIndexPercent.setForeground(zeroColor);
        }
    }

    private void initPane() {
        tbPane = new JBScrollPane();
        tbPane.setBorder(BorderFactory.createEmptyBorder());
        JPanel iPane = new JBPanel<>(new GridLayout(1, 4));
        iPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, JBColor.border()));
        iPane.add(cbIndex);
        iPane.add(lbIndexValue);
        iPane.add(lbIndexExtent);
        iPane.add(lbIndexPercent);
        cbIndex.addItemListener(i -> updateIndex());
        mPane = new JBPanel<>(new BorderLayout());
        mPane.add(tbPane, BorderLayout.CENTER);
        mPane.add(iPane, BorderLayout.SOUTH);
    }

    private static final String codeColumn = "Symbol";
    private static final String nameColumn = "Name";
    private static final String currentColumn = "Current";
    private static final String percentColumn = "Percentage";

    private void initTable() {
        tbBody = new JBTable();
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
                    tbBody.clearSelection();
                }
            }
        });
        tbModel = new StockerTableModel();
        tbBody.setShowVerticalLines(false);
        tbModel.setColumnIdentifiers(new String[]{codeColumn, nameColumn, currentColumn, percentColumn});
        tbBody.setModel(tbModel);
        tbBody.getTableHeader().setReorderingAllowed(false);
        tbBody.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        tbBody.getColumn(codeColumn).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        tbBody.getColumn(nameColumn).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        tbBody.getColumn(currentColumn).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                syncColorPatternSetting();
                setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
                String v = table.getValueAt(row, table.getColumn(percentColumn).getModelIndex()).toString();
                applyColorPatternToTable(v, this);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        tbBody.getColumn(percentColumn).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                syncColorPatternSetting();
                setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
                String v = value.toString();
                applyColorPatternToTable(v, this);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        tbPane.add(tbBody);
        tbPane.setViewportView(tbBody);
    }

    private void applyColorPatternToTable(String value, DefaultTableCellRenderer renderer) {
        if (value.startsWith("+")) {
            renderer.setForeground(upColor);
        } else if (value.startsWith("-")) {
            renderer.setForeground(downColor);
        } else {
            renderer.setForeground(zeroColor);
        }
    }

    public JComponent getComponent() {
        return mPane;
    }

    public JTable getTableBody() {
        return tbBody;
    }

    public DefaultTableModel getTableModel() {
        return tbModel;
    }

}
