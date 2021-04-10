package com.vermouthx.stocker.listeners;

import com.vermouthx.stocker.entities.StockerQuote;
import com.vermouthx.stocker.utils.StockerTableModelUtil;
import com.vermouthx.stocker.views.StockerTableView;

import javax.swing.table.DefaultTableModel;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class StockerQuoteUpdateListener implements StockerQuoteUpdateNotifier {
    private final StockerTableView myTableView;

    public StockerQuoteUpdateListener(StockerTableView myTableView) {
        this.myTableView = myTableView;
    }

    @Override
    public void syncQuotes(List<StockerQuote> quotes, int size) {
        DefaultTableModel tableModel = myTableView.getTableModel();
        quotes.forEach(quote -> {
            synchronized (myTableView.getTableModel()) {
                Vector<Object> vector = new Vector<>(Arrays.asList(quote.getCode(), quote.getName(), quote.getCurrent(), quote.getPercentage() + "%"));
                int rowIndex = StockerTableModelUtil.existAt(tableModel, quote.getCode());
                if (rowIndex != -1) {
                    tableModel.getDataVector().set(rowIndex, vector);
                    tableModel.fireTableRowsUpdated(rowIndex, rowIndex);
                } else {
                    if (quotes.size() == size) {
                        tableModel.addRow(vector);
                    }
                }
            }
        });
    }

    @Override
    public void syncIndices(List<StockerQuote> indices) {
        synchronized (myTableView) {
            myTableView.syncIndices(indices);
        }
    }

}
