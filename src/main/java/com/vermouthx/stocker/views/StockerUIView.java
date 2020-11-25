package com.vermouthx.stocker.views;

import com.intellij.ui.JBColor;
import com.vermouthx.stocker.enums.StockerQuoteColorPattern;
import com.vermouthx.stocker.settings.StockerSetting;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StockerUIView {
    private JPanel mPane;
    private JScrollPane sPane;
    private JTable tbView;
    private JButton btnAdd;
    private JButton btnDelete;
    private JButton btnRefresh;
    private JBColor upColor;
    private JBColor downColor;
    private final DefaultTableModel tbModel;

    public StockerUIView() {
        tbModel = new StockerTableModel();
        String[] columnNames = {"Code", "Name", "Opening", "High", "Low", "Current", "Percentage", "Update At"};
        tbModel.setColumnIdentifiers(columnNames);
        tbView.setModel(tbModel);
        tbView.getColumn("Current").setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                updateColorPattern();
                if (table.getValueAt(row, column + 1).toString().startsWith("+")) {
                    setForeground(upColor);
                } else {
                    setForeground(downColor);
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        tbView.getColumn("Percentage").setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                updateColorPattern();
                String v = value.toString();
                if (v.startsWith("+")) {
                    setForeground(upColor);
                } else {
                    setForeground(downColor);
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
    }

    private void updateColorPattern() {
        StockerSetting setting = StockerSetting.Companion.getInstance();
        if (setting.getQuoteColorPattern() == StockerQuoteColorPattern.RED_UP_GREEN_DOWN) {
            upColor = JBColor.RED;
            downColor = JBColor.GREEN;
        } else {
            upColor = JBColor.GREEN;
            downColor = JBColor.RED;
        }
    }

    public JPanel getContent() {
        return mPane;
    }

    public JTable getTbView() {
        return tbView;
    }

    public JButton getBtnAdd() {
        return btnAdd;
    }

    public JButton getBtnDelete() {
        return btnDelete;
    }

    public JButton getBtnRefresh() {
        return btnRefresh;
    }

    public DefaultTableModel getTbModel() {
        return tbModel;
    }
}
