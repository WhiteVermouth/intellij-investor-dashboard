package com.vermouthx.stocker.views;

import com.intellij.ui.JBColor;
import com.vermouthx.stocker.enums.StockerQuoteColorPattern;
import com.vermouthx.stocker.settings.StockerSetting;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class StockerTableView {
    private JPanel mPane;
    private JScrollPane tbPane;
    private JPanel dtPane;
    private JTable tbView;
    private JLabel lbDatetimeContent;
    private Color upColor;
    private Color downColor;
    private DefaultTableModel tbModel;

    public StockerTableView() {
        initPane();
        initTable();
        initDatetimeLabel();
    }

    private void initPane() {
        tbPane.setBorder(BorderFactory.createEmptyBorder());
        dtPane.setBorder(BorderFactory.createEmptyBorder(1, 10, 10, 10));
    }

    private void initTable() {
        tbModel = new StockerTableModel();
        String codeColumn = "Code";
        String nameColumn = "Name";
        String currentColumn = "Current";
        String percentColumn = "Percentage";
        tbModel.setColumnIdentifiers(new String[]{codeColumn, nameColumn, currentColumn, percentColumn});
        tbView.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                tbView.getSelectionModel().clearSelection();
            }
        });
        tbView.setModel(tbModel);
        tbView.getColumn(codeColumn).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        tbView.getColumn(nameColumn).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        tbView.getColumn(currentColumn).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                updateColorPattern();
                setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
                String v = table.getValueAt(row, column + 1).toString();
                applyColorPattern(v, this);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        tbView.getColumn(percentColumn).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                updateColorPattern();
                setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
                String v = value.toString();
                applyColorPattern(v, this);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
    }

    private void applyColorPattern(String value, DefaultTableCellRenderer renderer) {
        String grayColorHex = "#888888";
        if (value.startsWith("+")) {
            renderer.setForeground(upColor);
        } else if (value.startsWith("-")) {
            renderer.setForeground(downColor);
        } else {
            renderer.setForeground(JBColor.decode(grayColorHex));
        }
    }

    private void initDatetimeLabel() {
        String datetimeColorHex = "#6F84CA";
        lbDatetimeContent.setForeground(JBColor.decode(datetimeColorHex));
    }

    private void updateColorPattern() {
        String redColorHex = "#F44244";
        String greenColorHex = "#0EBB70";
        StockerSetting setting = StockerSetting.Companion.getInstance();
        if (setting.getQuoteColorPattern() == StockerQuoteColorPattern.RED_UP_GREEN_DOWN) {
            upColor = JBColor.decode(redColorHex);
            downColor = JBColor.decode(greenColorHex);
        } else {
            upColor = JBColor.decode(greenColorHex);
            downColor = JBColor.decode(redColorHex);
        }
    }

    public JComponent getComponent() {
        return mPane;
    }

    public DefaultTableModel getTbModel() {
        return tbModel;
    }

    public JLabel getLbDatetimeContent() {
        return lbDatetimeContent;
    }
}
