package com.vermouthx.stocker.listeners;

import com.vermouthx.stocker.entity.StockerStockQuote;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public class StockerQuoteListener implements StockerQuoteUpdateNotifier {
    private DefaultTableModel tableModel;

    public StockerQuoteListener(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
    }

    @Override
    public void after(List<StockerStockQuote> quotes) {
        tableModel.getDataVector().clear();
        quotes.forEach(quote -> {
            Object[] row = {
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

        });
        tableModel.fireTableDataChanged();
    }
}
