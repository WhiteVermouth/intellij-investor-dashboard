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
                if (!tableModel.getValueAt(rowIndex, 2).equals(quote.getOpening())) {
                    tableModel.setValueAt(quote.getOpening(), rowIndex, 2);
                    tableModel.fireTableCellUpdated(rowIndex, 2);
                }
                if (!tableModel.getValueAt(rowIndex, 3).equals(quote.getHigh())) {
                    tableModel.setValueAt(quote.getHigh(), rowIndex, 3);
                    tableModel.fireTableCellUpdated(rowIndex, 3);
                }
                if (!tableModel.getValueAt(rowIndex, 4).equals(quote.getLow())) {
                    tableModel.setValueAt(quote.getLow(), rowIndex, 4);
                    tableModel.fireTableCellUpdated(rowIndex, 4);
                }
                if (!tableModel.getValueAt(rowIndex, 5).equals(quote.getCurrent())) {
                    tableModel.setValueAt(quote.getCurrent(), rowIndex, 5);
                    tableModel.fireTableCellUpdated(rowIndex, 5);
                }
                if (!tableModel.getValueAt(rowIndex, 6).equals(quote.getPercentage())) {
                    tableModel.setValueAt(quote.getPercentage(), rowIndex, 6);
                    tableModel.fireTableCellUpdated(rowIndex, 6);
                }
                if (!tableModel.getValueAt(rowIndex, 7).equals(quote.getUpdateAt())) {
                    tableModel.setValueAt(quote.getUpdateAt(), rowIndex, 7);
                    tableModel.fireTableCellUpdated(rowIndex, 7);
                }
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
