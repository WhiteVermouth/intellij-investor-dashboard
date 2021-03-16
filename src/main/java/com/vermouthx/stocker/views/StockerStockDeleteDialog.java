package com.vermouthx.stocker.views;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.vermouthx.stocker.entities.StockerQuote;
import com.vermouthx.stocker.enums.StockerMarketType;
import com.vermouthx.stocker.settings.StockerSetting;
import com.vermouthx.stocker.utils.StockerQuoteHttpUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StockerStockDeleteDialog extends DialogWrapper {
    private final JPanel mPane = new JBPanel<>();
    private final JScrollPane tbPane = new JBScrollPane();
    private final JTable mTable = new JBTable();
    private final JComboBox<String> marketListMenu = new ComboBox<>();
    private final String[] tableColumnIdentifiers = new String[]{"Select", "Symbol", "Name"};
    private final DefaultTableModel tbModel = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 0;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? Boolean.class : super.getColumnClass(columnIndex);
        }
    };
    private final DefaultTableCellRenderer tableCellRenderer = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    };

    private StockerMarketType currentMarketSelection = StockerMarketType.AShare;

    public StockerStockDeleteDialog(Project project) {
        super(project, true);
        init();
        setTitle("Delete Stock Symbols");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        mPane.setLayout(new BorderLayout());
        initTable();
        initMenu();
        initMenuListener();
        mPane.setPreferredSize(new Dimension(400, 300));
        return mPane;
    }

    private void initMenu() {
        for (StockerMarketType m : StockerMarketType.values()) {
            marketListMenu.addItem(m.getDescription());
        }
        JPanel outer = new JBPanel<>(new FlowLayout(FlowLayout.LEFT));
        outer.add(new JBLabel("Market: "));
        outer.add(marketListMenu);
        mPane.add(outer, BorderLayout.NORTH);
    }

    private void initTable() {
        tbModel.setColumnIdentifiers(tableColumnIdentifiers);
        mTable.setModel(tbModel);
        mTable.getTableHeader().setDefaultRenderer(tableCellRenderer);
        mTable.getTableHeader().setReorderingAllowed(false);
        mTable.getColumn(tableColumnIdentifiers[0]).setPreferredWidth(20);
        for (int i = 1; i < tableColumnIdentifiers.length; i++) {
            mTable.getColumn(tableColumnIdentifiers[i]).setCellRenderer(tableCellRenderer);
        }
        mTable.setShowVerticalLines(false);
        mTable.setShowHorizontalLines(false);
        tbPane.add(mTable);
        tbPane.setViewportView(mTable);
        mPane.add(tbPane, BorderLayout.CENTER);
    }

    private void initMenuListener() {
        marketListMenu.addItemListener(e -> {
            String selectedMarket = (String) marketListMenu.getSelectedItem();
            StockerSetting setting = StockerSetting.Companion.getInstance();
            if (StockerMarketType.AShare.getDescription().equals(selectedMarket)) {
                currentMarketSelection = StockerMarketType.AShare;
                List<StockerQuote> quotes = StockerQuoteHttpUtil.INSTANCE.get(StockerMarketType.AShare, setting.getQuoteProvider(), setting.getAShareList());
                setupStockSymbols(quotes);
                return;
            }
            if (StockerMarketType.HKStocks.getDescription().equals(selectedMarket)) {
                currentMarketSelection = StockerMarketType.HKStocks;
                List<StockerQuote> quotes = StockerQuoteHttpUtil.INSTANCE.get(StockerMarketType.HKStocks, setting.getQuoteProvider(), setting.getHkStocksList());
                setupStockSymbols(quotes);
                return;
            }
            if (StockerMarketType.USStocks.getDescription().equals(selectedMarket)) {
                currentMarketSelection = StockerMarketType.USStocks;
                List<StockerQuote> quotes = StockerQuoteHttpUtil.INSTANCE.get(StockerMarketType.USStocks, setting.getQuoteProvider(), setting.getUsStocksList());
                setupStockSymbols(quotes);
            }
        });
    }

    public void setupStockSymbols(List<StockerQuote> symbols) {
        for (int row = tbModel.getRowCount() - 1; row >= 0; row--) {
            tbModel.removeRow(row);
        }
        for (StockerQuote symbol : symbols) {
            tbModel.addRow(new Object[]{false, symbol.getCode(), symbol.getName()});
        }
    }

    public List<String> deleteSymbols() {
        List<String> deletedSymbols = new ArrayList<>();
        StockerSetting setting = StockerSetting.Companion.getInstance();
        for (int row = 0; row < tbModel.getRowCount(); row++) {
            if ((boolean) tbModel.getValueAt(row, 0)) {
                setting.removeCode(currentMarketSelection, (String) tbModel.getValueAt(row, 1));
                deletedSymbols.add((String) tbModel.getValueAt(row, 1));
            }
        }
        return deletedSymbols;
    }

    public StockerMarketType getCurrentMarketSelection() {
        return currentMarketSelection;
    }
}
