package com.vermouthx.stocker.listeners;

import com.vermouthx.stocker.views.StockerTableView;

import javax.swing.table.DefaultTableModel;

public class StockerQuoteReloadListener implements StockerQuoteReloadNotifier {
    private final StockerTableView myTableView;

    public StockerQuoteReloadListener(StockerTableView myTableView) {
        this.myTableView = myTableView;
    }

    @Override
    public void clear() {
        DefaultTableModel tableModel = myTableView.getTableModel();
        synchronized (myTableView.getTableModel()) {
            // clear all table rows
            tableModel.setRowCount(0);
        }
    }
}
