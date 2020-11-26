package com.vermouthx.stocker.views;

import com.intellij.ui.JBColor;
import com.vermouthx.stocker.enums.StockerQuoteColorPattern;
import com.vermouthx.stocker.settings.StockerSetting;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StockerTableView {
    private JPanel mPane;
    private JScrollPane sPane;
    private JTable tbView;
    private JPanel tPane;
    private JLabel lbDatetimeNote;
    private JLabel lbDatetimeContent;
    private JBColor upColor;
    private JBColor downColor;
    private DefaultTableModel tbModel;

    private String codeColumn = "Code";
    private String nameColumn = "Name";
    private String currentColumn = "Current";
    private String percentColumn = "Percentage";

    public StockerTableView() {
        initPane();
        initTable();
        initDatetimeLabel();
    }

    private void initPane() {
        sPane.setBorder(BorderFactory.createEmptyBorder());
        tPane.setBorder(BorderFactory.createEmptyBorder(1, 10, 10, 10));
    }

    private void initTable() {
        tbModel = new StockerTableModel();
        tbModel.setColumnIdentifiers(new String[]{codeColumn, nameColumn, currentColumn, percentColumn});
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
                if (v.startsWith("+")) {
                    setForeground(upColor);
                } else {
                    setForeground(downColor);
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        tbView.getColumn(percentColumn).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                updateColorPattern();
                setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
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

    private void initDatetimeLabel() {
        lbDatetimeContent.setForeground(JBColor.CYAN);
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

    public DefaultTableModel getTbModel() {
        return tbModel;
    }

    public JLabel getLbDatetimeContent() {
        return lbDatetimeContent;
    }
}
