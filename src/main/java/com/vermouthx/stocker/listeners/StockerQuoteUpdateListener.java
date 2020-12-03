package com.vermouthx.stocker.listeners;

import com.vermouthx.stocker.entities.StockerQuote;
import com.vermouthx.stocker.utils.StockerTableModelUtil;
import com.vermouthx.stocker.views.StockerTableView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class StockerQuoteUpdateListener implements StockerQuoteUpdateNotifier {
    private final StockerTableView allTableView;
    private final StockerTableView myTableView;


    public StockerQuoteUpdateListener(StockerTableView allTableView, StockerTableView myTableView) {
        this.allTableView = allTableView;
        this.myTableView = myTableView;
    }

    @Override
    public void after(List<StockerQuote> quotes) {
        if (quotes != null) {
            quotes.forEach(quote -> {
                synchronized (myTableView.getTableModel()) {
                    this.refreshTableModel(myTableView.getTableModel(), quote);
                }
                synchronized (allTableView.getTableModel()) {
                    this.refreshTableModel(allTableView.getTableModel(), quote);
                }
            });
            if (!quotes.isEmpty()) {
                String updateAt = quotes.get(0).getUpdateAt();
                JLabel dtLabel = myTableView.getLbDatetimeContent();
                dtLabel.setText(updateAt);
                allTableView.getLbDatetimeContent().setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            }
        }
    }

    private void refreshTableModel(DefaultTableModel tableModel, StockerQuote quote) {
        int rowIndex = StockerTableModelUtil.existAt(tableModel, quote.getCode());
        if (rowIndex != -1) {
            if (!tableModel.getValueAt(rowIndex, 1).equals(quote.getName())) {
                tableModel.setValueAt(quote.getName(), rowIndex, 1);
                tableModel.fireTableCellUpdated(rowIndex, 1);
            }
            if (!tableModel.getValueAt(rowIndex, 2).equals(quote.getCurrent())) {
                tableModel.setValueAt(quote.getCurrent(), rowIndex, 2);
                tableModel.fireTableCellUpdated(rowIndex, 2);
            }
            if (!tableModel.getValueAt(rowIndex, 3).equals(quote.getPercentage())) {
                tableModel.setValueAt(quote.getPercentage(), rowIndex, 3);
                tableModel.fireTableCellUpdated(rowIndex, 3);
            }
        } else {
            tableModel.addRow(new String[]{quote.getCode(), quote.getName(), quote.getCurrent(), quote.getPercentage()});
        }
    }
}
