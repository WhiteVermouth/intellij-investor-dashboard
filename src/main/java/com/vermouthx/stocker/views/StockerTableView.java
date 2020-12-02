package com.vermouthx.stocker.views;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.vermouthx.stocker.enums.StockerQuoteColorPattern;
import com.vermouthx.stocker.settings.StockerSetting;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class StockerTableView {
    private Logger log = Logger.getInstance(getClass());

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
        mPane = new JBPanel<>(new BorderLayout());
        tbPane = new JBScrollPane();
        dtPane = new JBPanel<>(new FlowLayout());
        tbPane.setBorder(BorderFactory.createEmptyBorder());
        dtPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, JBColor.border()));
        mPane.add(tbPane, BorderLayout.CENTER);
        mPane.add(dtPane, BorderLayout.SOUTH);
    }

    private void initTable() {
        String codeColumn = "Code";
        String nameColumn = "Name";
        String currentColumn = "Current";
        String percentColumn = "Percentage";
        tbView = new JBTable();
        tbModel = new StockerTableModel();
        tbPane.add(tbView);
        tbPane.setViewportView(tbView);
        tbModel.setColumnIdentifiers(new String[]{codeColumn, nameColumn, currentColumn, percentColumn});
        tbView.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                tbView.getSelectionModel().clearSelection();
            }
        });
        tbView.setModel(tbModel);
        tbView.getTableHeader().setReorderingAllowed(false);
        tbView.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
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
                String v = table.getValueAt(row, table.getColumn(percentColumn).getModelIndex()).toString();
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
            renderer.setBackground(upColor);
        } else if (value.startsWith("-")) {
            renderer.setBackground(downColor);
        } else {
            renderer.setBackground(JBColor.decode(grayColorHex));
        }
        renderer.setForeground(JBColor.decode("#F8F8F2"));
    }

    private void initDatetimeLabel() {
        lbDatetimeContent = new JBLabel();
        String datetimeColorHex = "#6F84CA";
        lbDatetimeContent.setForeground(JBColor.decode(datetimeColorHex));
        dtPane.add(new JBLabel("Last update at: "));
        dtPane.add(lbDatetimeContent);
    }

    private void updateColorPattern() {
        String redColorHex = "#E93F41";
        String greenColorHex = "#0DB06A";
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
