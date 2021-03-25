package com.vermouthx.stocker.views;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.vermouthx.stocker.StockerApp;
import com.vermouthx.stocker.entities.StockerSuggest;
import com.vermouthx.stocker.enums.StockerMarketType;
import com.vermouthx.stocker.enums.StockerStockOperation;
import com.vermouthx.stocker.settings.StockerSetting;
import com.vermouthx.stocker.utils.StockerActionUtil;
import com.vermouthx.stocker.utils.StockerSuggestHttpUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.List;

public class StockerStockAddDialog extends DialogWrapper {
    private final JPanel mPane = new JPanel(new BorderLayout());
    private final JBScrollPane container = new JBScrollPane();
    private final SearchTextField searchTextField = new SearchTextField(true);

    private final Project project;

    public StockerStockAddDialog(Project project) {
        super(project);
        this.project = project;
        init();
        setTitle("Search Stocks");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        initSearchBar();
        initSearchBarListener();
        mPane.add(container, BorderLayout.CENTER);
//        mPane.setMaximumSize(new Dimension(400, 400));
        mPane.setPreferredSize(new Dimension(400, 400));
        setupStockSymbols(StockerSuggestHttpUtil.INSTANCE.suggest("SH600"));
        return mPane;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{};
    }

    private void initSearchBar() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.add(searchTextField, BorderLayout.CENTER);
        outer.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        mPane.add(outer, BorderLayout.NORTH);
    }

    private void initSearchBarListener() {
        searchTextField.addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                new Thread(() -> {
                    String text = searchTextField.getText();
                    if (text != null && !text.equals("")) {
                        List<StockerSuggest> suggests = StockerSuggestHttpUtil.INSTANCE.suggest(text);
                        setupStockSymbols(suggests);
                    }
                }).start();
            }
        });
    }

    public synchronized void setupStockSymbols(List<StockerSuggest> suggests) {
        JPanel inner = new JPanel();
        inner.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        inner.setLayout(new GridLayout(0, 1));
        StockerSetting setting = StockerSetting.Companion.getInstance();
        for (StockerSuggest suggest : suggests) {
            GridBagLayout layout = new GridBagLayout();
            JPanel rowPane = new JPanel(layout);
            rowPane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border()));
            String code = suggest.getCode();
            String name = suggest.getName();
            JBLabel lbCode = new JBLabel(code);
            JBLabel lbName = new JBLabel(name);
            JButton btnOperation = new JButton();
            if (setting.containsCode(code)) {
                btnOperation.setText(StockerStockOperation.STOCK_DELETE.getOperation());
            } else {
                btnOperation.setText(StockerStockOperation.STOCK_ADD.getOperation());
            }
            StockerMarketType market = suggest.getMarket();
            btnOperation.addActionListener(e -> {
                StockerApp.INSTANCE.shutdown();
                String txt = btnOperation.getText();
                StockerStockOperation operation = StockerStockOperation.mapOf(txt);
                switch (operation) {
                    case STOCK_ADD:
                        if (StockerActionUtil.addStock(market, suggest, project)) {
                            btnOperation.setText(StockerStockOperation.STOCK_DELETE.getOperation());
                        }
                        break;
                    case STOCK_DELETE:
                        if (StockerActionUtil.removeStock(market, suggest)) {
                            btnOperation.setText(StockerStockOperation.STOCK_ADD.getOperation());
                        }

                }
                StockerApp.INSTANCE.schedule();
            });
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.WEST;
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 0.5;
            c.weighty = 0.1;
            rowPane.add(lbCode, c);
            c.anchor = GridBagConstraints.CENTER;
            c.gridx = 1;
            c.gridy = 0;
            c.gridwidth = 2;
            c.gridheight = 1;
            c.weightx = 0.5;
            c.weighty = 0.1;
            rowPane.add(lbName, c);
            c.anchor = GridBagConstraints.EAST;
            c.gridx = 3;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 0.5;
            c.weighty = 0.1;
            rowPane.add(btnOperation, c);
            inner.add(rowPane);
        }
        container.setViewportView(inner);
    }
}
