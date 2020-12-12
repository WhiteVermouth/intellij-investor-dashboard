package com.vermouthx.stocker.listeners;

import com.vermouthx.stocker.utils.StockerTableModelUtil;
import com.vermouthx.stocker.views.StockerTableView;

import javax.swing.table.DefaultTableModel;

public class StockerQuoteDeleteListener implements StockerQuoteDeleteNotifier {

    private final StockerTableView myTableView;

    public StockerQuoteDeleteListener(StockerTableView myTableView) {
        this.myTableView = myTableView;
    }

    @Override
    public void after(String code) {
        synchronized (myTableView.getTableModel()) {
            DefaultTableModel tableModel = myTableView.getTableModel();
            int rowIndex = StockerTableModelUtil.existAt(tableModel, code);
            if (rowIndex != -1) {
                tableModel.removeRow(rowIndex);
                tableModel.fireTableRowsDeleted(rowIndex, rowIndex);
            }
        }
    }

}
