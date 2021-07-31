package com.vermouthx.stocker.views;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.vermouthx.stocker.StockerApp;
import com.vermouthx.stocker.StockerAppManager;
import com.vermouthx.stocker.entities.StockerQuote;
import com.vermouthx.stocker.entities.StockerSuggest;
import com.vermouthx.stocker.enums.StockerMarketType;
import com.vermouthx.stocker.enums.StockerStockOperation;
import com.vermouthx.stocker.settings.StockerSetting;
import com.vermouthx.stocker.utils.StockerActionUtil;
import com.vermouthx.stocker.utils.StockerQuoteHttpUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockerStockDeleteDialog extends DialogWrapper {
    private final JPanel mPane = new JBPanel<>(new BorderLayout());
    private final JTabbedPane tabbedPane = new JBTabbedPane();
    private final Map<Integer, JScrollPane> tabMap = new HashMap<>();
    private StockerMarketType currentMarketSelection = StockerMarketType.AShare;

    private final Project project;

    public StockerStockDeleteDialog(Project project) {
        super(project);
        this.project = project;
        init();
        setTitle("Manage Stocks");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        tabbedPane.add("CN", createTabContent(0));
        tabbedPane.add("HK", createTabContent(1));
        tabbedPane.add("US", createTabContent(2));
        tabbedPane.add("Crypto", createTabContent(3));
        tabbedPane.addChangeListener(e -> {
            String selectedMarket = null;
            switch (tabbedPane.getSelectedIndex()) {
                case 0:
                    selectedMarket = StockerMarketType.AShare.getTitle();
                    break;
                case 1:
                    selectedMarket = StockerMarketType.HKStocks.getTitle();
                    break;
                case 2:
                    selectedMarket = StockerMarketType.USStocks.getTitle();
                    break;
                case 3:
                    selectedMarket = StockerMarketType.Crypto.getTitle();
            }
            StockerSetting setting = StockerSetting.Companion.getInstance();
            if (StockerMarketType.AShare.getTitle().equals(selectedMarket)) {
                currentMarketSelection = StockerMarketType.AShare;
                List<StockerQuote> quotes = StockerQuoteHttpUtil.INSTANCE.get(StockerMarketType.AShare, setting.getQuoteProvider(), setting.getAShareList());
                setupStockSymbols(quotes);
                return;
            }
            if (StockerMarketType.HKStocks.getTitle().equals(selectedMarket)) {
                currentMarketSelection = StockerMarketType.HKStocks;
                List<StockerQuote> quotes = StockerQuoteHttpUtil.INSTANCE.get(StockerMarketType.HKStocks, setting.getQuoteProvider(), setting.getHkStocksList());
                setupStockSymbols(quotes);
                return;
            }
            if (StockerMarketType.USStocks.getTitle().equals(selectedMarket)) {
                currentMarketSelection = StockerMarketType.USStocks;
                List<StockerQuote> quotes = StockerQuoteHttpUtil.INSTANCE.get(StockerMarketType.USStocks, setting.getQuoteProvider(), setting.getUsStocksList());
                setupStockSymbols(quotes);
            }
            if (StockerMarketType.Crypto.getTitle().equals(selectedMarket)) {
                currentMarketSelection = StockerMarketType.Crypto;
                List<StockerQuote> quotes = StockerQuoteHttpUtil.INSTANCE.get(StockerMarketType.Crypto, setting.getQuoteProvider(), setting.getCryptoList());
                setupStockSymbols(quotes);
            }
        });
        mPane.add(tabbedPane, BorderLayout.CENTER);
        mPane.setMaximumSize(new Dimension(400, 500));
        mPane.setPreferredSize(new Dimension(400, 500));
        return mPane;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{};
    }

    private JScrollPane createTabContent(int index) {
        JScrollPane container = new JBScrollPane();
        tabMap.put(index, container);
        return container;
    }

    public void setupStockSymbols(List<StockerQuote> symbols) {
        JScrollPane container = tabMap.get(tabbedPane.getSelectedIndex());
        JPanel inner = new JBPanel<>();
        inner.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        for (StockerQuote symbol : symbols) {
            JPanel row = new JBPanel<>(new GridLayout(1, 3));
            row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border()));
            row.setMaximumSize(new Dimension(400, 30));
            JLabel lbCode = new JBLabel(symbol.getCode());
            String name = symbol.getName();
            String lbNameText = name;
            if (name.length() > 7) {
                lbNameText = name.substring(0, 7) + "...";
            }
            JLabel lbName = new JBLabel(lbNameText);
            JButton btnOperation = new JButton(StockerStockOperation.STOCK_DELETE.getOperation());
            btnOperation.addActionListener(e -> {
                StockerApp myApplication = StockerAppManager.INSTANCE.getMyApplicationMap().get(project);
                if (myApplication != null) {
                    myApplication.shutdown();
                    String txt = btnOperation.getText();
                    StockerStockOperation operation = StockerStockOperation.mapOf(txt);
                    switch (operation) {
                        case STOCK_ADD:
                            if (StockerActionUtil.addStock(currentMarketSelection, new StockerSuggest(symbol.getCode(), symbol.getName(), currentMarketSelection), project)) {
                                btnOperation.setText(StockerStockOperation.STOCK_DELETE.getOperation());
                            }
                            break;
                        case STOCK_DELETE:
                            if (StockerActionUtil.removeStock(currentMarketSelection, new StockerSuggest(symbol.getCode(), symbol.getName(), currentMarketSelection))) {
                                btnOperation.setText(StockerStockOperation.STOCK_ADD.getOperation());
                            }
                    }
                    myApplication.schedule();
                }
            });
            row.add(lbCode);
            row.add(lbName);
            row.add(btnOperation);
            inner.add(row);
        }
        container.setViewportView(inner);
    }
}
