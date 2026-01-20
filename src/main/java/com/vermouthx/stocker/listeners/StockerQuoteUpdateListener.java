package com.vermouthx.stocker.listeners;

import com.vermouthx.stocker.entities.StockerQuote;
import com.vermouthx.stocker.settings.StockerSetting;
import com.vermouthx.stocker.utils.StockerTableModelUtil;
import com.vermouthx.stocker.views.StockerTableView;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public class StockerQuoteUpdateListener implements StockerQuoteUpdateNotifier {
    private final StockerTableView myTableView;

    public StockerQuoteUpdateListener(StockerTableView myTableView) {
        this.myTableView = myTableView;
    }

    @Override
    public void syncQuotes(List<StockerQuote> quotes, int size) {
        DefaultTableModel tableModel = myTableView.getTableModel();
        StockerSetting setting = StockerSetting.Companion.getInstance();
        
        quotes.forEach(quote -> {
            synchronized (myTableView.getTableModel()) {
                String displayName = setting.getDisplayName(quote.getCode(), quote.getName());
                int rowIndex = StockerTableModelUtil.existAt(tableModel, quote.getCode());
                if (rowIndex != -1) {
                    if (!tableModel.getValueAt(rowIndex, 1).equals(displayName)) {
                        tableModel.setValueAt(displayName, rowIndex, 1);
                        tableModel.fireTableCellUpdated(rowIndex, 1);
                    }
                    if (!tableModel.getValueAt(rowIndex, 2).equals(quote.getCurrent())) {
                        tableModel.setValueAt(quote.getCurrent(), rowIndex, 2);
                        tableModel.fireTableCellUpdated(rowIndex, 2);
                    }
                    if (!tableModel.getValueAt(rowIndex, 3).equals(quote.getPercentage())) {
                        tableModel.setValueAt(quote.getPercentage() + "%", rowIndex, 3);
                        tableModel.fireTableCellUpdated(rowIndex, 3);
                    }
                } else {
                    if (quotes.size() == size) {
                        tableModel.addRow(new Object[]{quote.getCode(), displayName, quote.getCurrent(), quote.getPercentage() + "%"});
                        // Clear sort state when new rows are added
                        myTableView.clearSortState();
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
