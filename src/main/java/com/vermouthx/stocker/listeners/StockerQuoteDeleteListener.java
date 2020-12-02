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
        DefaultTableModel myTableModel = myTableView.getTbModel();
        DefaultTableModel allTableModel = allTableView.getTbModel();
        int myRowIndex = StockerTableModelUtil.existAt(myTableModel, code);
        int allRowIndex = StockerTableModelUtil.existAt(allTableModel, code);
        if (myRowIndex != -1) {
            myTableModel.removeRow(myRowIndex);
            myTableModel.fireTableRowsDeleted(myRowIndex, myRowIndex);
            allTableModel.removeRow(allRowIndex);
            allTableModel.fireTableRowsDeleted(allRowIndex, allRowIndex);
        }
    }
}
