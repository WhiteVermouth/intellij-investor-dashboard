package com.vermouthx.stocker.views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class StockerUIView {
    private JPanel mPane;
    private JScrollPane sPane;
    private JTable tbView;
    private JButton btnAdd;
    private JButton btnRefresh;
    private final DefaultTableModel tbModel;

    public StockerUIView() {
        btnRefresh.setVisible(false);
        tbModel = new StockerTableModel();
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> {

        });
        popupMenu.add(deleteItem);
        tbView.setComponentPopupMenu(popupMenu);
        String[] columnNames = {"Code", "Name", "Opening", "High", "Low", "Current", "Percentage", "Update At"};
        tbModel.setColumnIdentifiers(columnNames);
        tbView.setModel(tbModel);
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

    public JButton getBtnRefresh() {
        return btnRefresh;
    }

    public DefaultTableModel getTbModel() {
        return tbModel;
    }
}
