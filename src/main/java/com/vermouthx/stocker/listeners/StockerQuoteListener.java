package com.vermouthx.stocker.listeners;

import com.vermouthx.stocker.entities.StockerQuote;
import com.vermouthx.stocker.views.StockerTableView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Date;
import java.util.List;

public class StockerQuoteListener implements StockerQuoteUpdateNotifier {
    private final StockerTableView tableView;

    public StockerQuoteListener(StockerTableView tableView) {
        this.tableView = tableView;
    }

    @Override
    public void after(List<StockerQuote> quotes) {
        DefaultTableModel tableModel = tableView.getTbModel();
        if (tableModel.getRowCount() > quotes.size()) {
            tableModel.getDataVector().clear();
            tableModel.fireTableDataChanged();
        }
        quotes.forEach(quote -> {
            int rowIndex = existAt(tableModel, quote.getCode());
            if (rowIndex != -1) {
                if (!tableModel.getValueAt(rowIndex, 2).equals(quote.getCurrent())) {
                    tableModel.setValueAt(quote.getCurrent(), rowIndex, 2);
                    tableModel.fireTableCellUpdated(rowIndex, 2);
                }
                if (!tableModel.getValueAt(rowIndex, 3).equals(quote.getPercentage())) {
                    tableModel.setValueAt(quote.getPercentage(), rowIndex, 3);
                    tableModel.fireTableCellUpdated(rowIndex, 3);
                }
            } else {
                String[] row = {quote.getCode(), quote.getName(), quote.getCurrent(), quote.getPercentage()};
                tableModel.addRow(row);
            }
        });
        JLabel dtLabel = tableView.getLbDatetimeContent();
        dtLabel.setText(new Date().toString());
    }

    private int existAt(DefaultTableModel tableModel, String code) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String c = tableModel.getValueAt(i, 0).toString();
            if (code != null && code.equals(c)) {
                return i;
            }
        }
        return -1;
    }
}
