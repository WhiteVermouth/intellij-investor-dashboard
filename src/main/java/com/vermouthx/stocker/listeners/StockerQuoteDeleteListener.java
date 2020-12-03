package com.vermouthx.stocker.listeners;

import com.vermouthx.stocker.utils.StockerTableModelUtil;
import com.vermouthx.stocker.views.StockerTableView;

import javax.swing.table.DefaultTableModel;

public class StockerQuoteDeleteListener implements StockerQuoteDeleteNotifier {

    private final StockerTableView allTableView;
    private final StockerTableView myTableView;

    public StockerQuoteDeleteListener(StockerTableView allTableView, StockerTableView myTableView) {
        this.allTableView = allTableView;
        this.myTableView = myTableView;
    }

    @Override
    public void after(String code) {
        synchronized (myTableView.getTableModel()) {
            refreshTableModel(myTableView.getTableModel(), code);
        }
        synchronized (allTableView.getTableModel()) {
            refreshTableModel(allTableView.getTableModel(), code);
        }
    }

    private void refreshTableModel(DefaultTableModel tableModel, String code) {
        int rowIndex = StockerTableModelUtil.existAt(tableModel, code);
        if (rowIndex != -1) {
            tableModel.removeRow(rowIndex);
            tableModel.fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }
}
