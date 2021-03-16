package com.vermouthx.stocker.views;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.vermouthx.stocker.entities.StockerSuggest;
import com.vermouthx.stocker.enums.StockerMarketType;
import com.vermouthx.stocker.settings.StockerSetting;
import com.vermouthx.stocker.utils.StockerSuggestHttpUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

public class StockerStockAddDialog extends DialogWrapper {
    private final JPanel mPane = new JBPanel<>(new BorderLayout());
    private final JScrollPane tbPane = new JBScrollPane();
    private final JTable mTable = new JBTable();
    private final SearchTextField searchTextField = new SearchTextField(true);
    private final String[] tableColumnIdentifiers = new String[]{"Symbol", "Name", "Market", "Operation"};
    private final DefaultTableModel tbModel = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 3;
        }
    };
    private final DefaultTableCellRenderer tableCellRenderer = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    };

    private final TableCellRenderer tableCellButtonRender = new TableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return (JButton) value;
        }
    };

    public StockerStockAddDialog(Project project) {
        super(project, true);
        init();
        setTitle("Add Stock Symbols");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        initTable();
        initSearchBar();
        initSearchBarListener();
        mPane.setPreferredSize(new Dimension(400, 300));
        return mPane;
    }

    private void initTable() {
        tbModel.setColumnIdentifiers(tableColumnIdentifiers);
        mTable.setModel(tbModel);
        mTable.getTableHeader().setDefaultRenderer(tableCellRenderer);
        mTable.getTableHeader().setReorderingAllowed(false);
        for (int i = 0; i < tableColumnIdentifiers.length - 1; i++) {
            mTable.getColumn(tableColumnIdentifiers[i]).setCellRenderer(tableCellRenderer);
        }
        mTable.getColumn(tableColumnIdentifiers[tableColumnIdentifiers.length - 1]).setCellRenderer(tableCellButtonRender);
        mTable.setShowVerticalLines(false);
        mTable.setShowHorizontalLines(false);
        tbPane.add(mTable);
        tbPane.setViewportView(mTable);
        mPane.add(tbPane, BorderLayout.CENTER);
    }

    private void initSearchBar() {
        mPane.add(searchTextField, BorderLayout.NORTH);
    }

    private Thread currentSearchThread;

    private void initSearchBarListener() {
        searchTextField.addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                if (currentSearchThread != null) {
                    currentSearchThread.interrupt();
                }
                currentSearchThread = new Thread(() -> {
                    String text = searchTextField.getText();
                    List<StockerSuggest> suggests = StockerSuggestHttpUtil.INSTANCE.suggest(text);
                    setupStockSymbols(suggests);
                });
                currentSearchThread.start();
            }
        });
    }

    public void setupStockSymbols(List<StockerSuggest> suggests) {
        synchronized (tbModel) {
            for (int row = tbModel.getRowCount() - 1; row >= 0; row--) {
                tbModel.removeRow(row);
            }
            StockerSetting setting = StockerSetting.Companion.getInstance();
            for (StockerSuggest suggest : suggests) {
                String code = suggest.getCode().toUpperCase();
                String name = suggest.getName();
                StockerMarketType market = suggest.getMarket();
                JButton button = new JButton("ADD");
                if (setting.containsCode(code)) {
                    button.setText("ADDED");
                    button.setEnabled(false);
                }
                button.addActionListener(e -> {
                    switch (market) {
                        case AShare:
                            setting.getAShareList().add(code);
                            break;
                        case HKStocks:
                            setting.getHkStocksList().add(code);
                            break;
                        case USStocks:
                            setting.getUsStocksList().add(code);
                            break;
                    }
                    button.setText("ADDED");
                    button.setEnabled(false);
                });
                tbModel.addRow(new Object[]{code, name, market.getTitle(), button});
            }
        }
    }
}
