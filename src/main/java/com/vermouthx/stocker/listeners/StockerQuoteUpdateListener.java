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
                    // Update existing row - check each column
                    // Column 0: Code (doesn't change)
                    // Column 1: Name
                    if (!tableModel.getValueAt(rowIndex, 1).equals(displayName)) {
                        tableModel.setValueAt(displayName, rowIndex, 1);
                        tableModel.fireTableCellUpdated(rowIndex, 1);
                    }
                    // Column 2: Current
                    if (!tableModel.getValueAt(rowIndex, 2).equals(quote.getCurrent())) {
                        tableModel.setValueAt(quote.getCurrent(), rowIndex, 2);
                        tableModel.fireTableCellUpdated(rowIndex, 2);
                    }
                    // Column 3: Opening
                    if (!tableModel.getValueAt(rowIndex, 3).equals(quote.getOpening())) {
                        tableModel.setValueAt(quote.getOpening(), rowIndex, 3);
                        tableModel.fireTableCellUpdated(rowIndex, 3);
                    }
                    // Column 4: Close
                    if (!tableModel.getValueAt(rowIndex, 4).equals(quote.getClose())) {
                        tableModel.setValueAt(quote.getClose(), rowIndex, 4);
                        tableModel.fireTableCellUpdated(rowIndex, 4);
                    }
                    // Column 5: Low
                    if (!tableModel.getValueAt(rowIndex, 5).equals(quote.getLow())) {
                        tableModel.setValueAt(quote.getLow(), rowIndex, 5);
                        tableModel.fireTableCellUpdated(rowIndex, 5);
                    }
                    // Column 6: High
                    if (!tableModel.getValueAt(rowIndex, 6).equals(quote.getHigh())) {
                        tableModel.setValueAt(quote.getHigh(), rowIndex, 6);
                        tableModel.fireTableCellUpdated(rowIndex, 6);
                    }
                    // Column 7: Change
                    if (!tableModel.getValueAt(rowIndex, 7).equals(quote.getChange())) {
                        tableModel.setValueAt(quote.getChange(), rowIndex, 7);
                        tableModel.fireTableCellUpdated(rowIndex, 7);
                    }
                    // Column 8: Change%
                    if (!tableModel.getValueAt(rowIndex, 8).equals(quote.getPercentage())) {
                        tableModel.setValueAt(quote.getPercentage() + "%", rowIndex, 8);
                        tableModel.fireTableCellUpdated(rowIndex, 8);
                    }
                    // Column 9: Cost Price (user-set, read from settings)
                    Double costPrice = setting.getCostPrice(quote.getCode());
                    String costPriceStr = costPrice != null ? String.format("%.3f", costPrice) : "-";
                    if (!costPriceStr.equals(tableModel.getValueAt(rowIndex, 9))) {
                        tableModel.setValueAt(costPriceStr, rowIndex, 9);
                        tableModel.fireTableCellUpdated(rowIndex, 9);
                    }
                    // Column 10: Holdings (user-set, read from settings)
                    Integer holdings = setting.getHoldings(quote.getCode());
                    Object holdingsVal = holdings != null ? holdings : "-";
                    if (!holdingsVal.equals(tableModel.getValueAt(rowIndex, 10))) {
                        tableModel.setValueAt(holdingsVal, rowIndex, 10);
                        tableModel.fireTableCellUpdated(rowIndex, 10);
                    }
                } else {
                    if (quotes.size() == size) {
                        Double costPrice = setting.getCostPrice(quote.getCode());
                        Integer holdings = setting.getHoldings(quote.getCode());
                        tableModel.addRow(new Object[]{
                            quote.getCode(), 
                            displayName, 
                            quote.getCurrent(), 
                            quote.getOpening(), 
                            quote.getClose(), 
                            quote.getLow(), 
                            quote.getHigh(), 
                            quote.getChange(), 
                            quote.getPercentage() + "%",
                            costPrice != null ? String.format("%.3f", costPrice) : "-",
                            holdings != null ? holdings : "-"
                        });
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
