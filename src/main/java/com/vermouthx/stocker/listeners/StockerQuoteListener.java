package com.vermouthx.stocker.listeners;

import com.vermouthx.stocker.entities.StockerStockQuote;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public class StockerQuoteListener implements StockerQuoteUpdateNotifier {
    private final DefaultTableModel tableModel;

    public StockerQuoteListener(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
    }

    @Override
    public void after(List<StockerStockQuote> quotes) {
        quotes.forEach(quote -> {
            int rowIndex = existAt(quote.getCode());
            if (rowIndex != -1) {
                tableModel.setValueAt(quote.getOpening(), rowIndex, 2);
                tableModel.setValueAt(quote.getHigh(), rowIndex, 3);
                tableModel.setValueAt(quote.getLow(), rowIndex, 4);
                tableModel.setValueAt(quote.getCurrent(), rowIndex, 5);
                tableModel.setValueAt(quote.getPercentage(), rowIndex, 6);
                tableModel.setValueAt(quote.getUpdateAt(), rowIndex, 7);
            } else {
                String[] row = {
                        quote.getCode().toUpperCase(),
                        quote.getName(),
                        quote.getOpening(),
                        quote.getHigh(),
                        quote.getLow(),
                        quote.getCurrent(),
                        quote.getPercentage(),
                        quote.getUpdateAt()
                };
                tableModel.addRow(row);
            }
            tableModel.fireTableDataChanged();
        });
    }

    private int existAt(String code) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String c = tableModel.getValueAt(i, 0).toString();
            if (code != null && code.equals(c)) {
                return i;
            }
        }
        return -1;
    }
}
