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
            DefaultTableModel myTableModel = myTableView.getTbModel();
            DefaultTableModel allTableModel = allTableView.getTbModel();
            quotes.forEach(quote -> {
                int myRowIndex = StockerTableModelUtil.existAt(myTableModel, quote.getCode());
                int allRowIndex = StockerTableModelUtil.existAt(allTableModel, quote.getCode());
                if (myRowIndex != -1) {
                    if (!myTableModel.getValueAt(myRowIndex, 1).equals(quote.getName())) {
                        myTableModel.setValueAt(quote.getName(), myRowIndex, 1);
                        myTableModel.fireTableCellUpdated(myRowIndex, 1);
                        allTableModel.setValueAt(quote.getName(), allRowIndex, 1);
                        allTableModel.fireTableCellUpdated(allRowIndex, 1);
                    }
                    if (!myTableModel.getValueAt(myRowIndex, 2).equals(quote.getCurrent())) {
                        myTableModel.setValueAt(quote.getCurrent(), myRowIndex, 2);
                        myTableModel.fireTableCellUpdated(myRowIndex, 2);
                        allTableModel.setValueAt(quote.getCurrent(), allRowIndex, 2);
                        allTableModel.fireTableCellUpdated(allRowIndex, 2);
                    }
                    if (!myTableModel.getValueAt(myRowIndex, 3).equals(quote.getPercentage())) {
                        myTableModel.setValueAt(quote.getPercentage(), myRowIndex, 3);
                        myTableModel.fireTableCellUpdated(myRowIndex, 3);
                        allTableModel.setValueAt(quote.getPercentage(), allRowIndex, 3);
                        allTableModel.fireTableCellUpdated(allRowIndex, 3);
                    }
                } else {
                    String[] row = {quote.getCode(), quote.getName(), quote.getCurrent(), quote.getPercentage()};
                    myTableModel.addRow(row);
                    allTableModel.addRow(row);
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
}
