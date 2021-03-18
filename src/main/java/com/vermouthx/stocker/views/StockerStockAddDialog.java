package com.vermouthx.stocker.views;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.vermouthx.stocker.entities.StockerSuggest;
import com.vermouthx.stocker.enums.StockerMarketType;
import com.vermouthx.stocker.settings.StockerSetting;
import com.vermouthx.stocker.utils.StockerSuggestHttpUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.List;

public class StockerStockAddDialog extends DialogWrapper {
    private final JPanel mPane = new JBPanel<>(new BorderLayout());
    private final JScrollPane container = new JBScrollPane();
    private final SearchTextField searchTextField = new SearchTextField(true);

    public StockerStockAddDialog(Project project) {
        super(project);
        init();
        setTitle("Search Stocks");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        initSearchBar();
        initSearchBarListener();
        mPane.add(container, BorderLayout.CENTER);
        mPane.setMaximumSize(new Dimension(300, 400));
        mPane.setPreferredSize(new Dimension(300, 400));
        return mPane;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{};
    }

    private void initSearchBar() {
        JPanel outer = new JBPanel<>(new BorderLayout());
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
                    List<StockerSuggest> suggests = StockerSuggestHttpUtil.INSTANCE.suggest(text);
                    setupStockSymbols(suggests);
                }).start();
            }
        });
    }

    public synchronized void setupStockSymbols(List<StockerSuggest> suggests) {
        JPanel inner = new JBPanel<>();
        inner.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        StockerSetting setting = StockerSetting.Companion.getInstance();
        for (StockerSuggest suggest : suggests) {
            GridLayout layout = new GridLayout(1, 4);
            JPanel row = new JBPanel<>(layout);
            row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border()));
            row.setMaximumSize(new Dimension(400, 30));
            String code = suggest.getCode().toUpperCase();
            String name = suggest.getName();
            if (name.length() > 6) {
                name = name.substring(0, 6) + "...";
            }
            StockerMarketType market = suggest.getMarket();
            JLabel lbCode = new JBLabel(code);
            row.add(lbCode);
            JLabel lbName = new JBLabel(name);
            row.add(lbName);
            JLabel lbMarket = new JBLabel(market.getTitle());
            row.add(lbMarket);
            JButton operation = new JButton("Add");
            if (setting.containsCode(code)) {
                operation.setEnabled(false);
            }
            operation.addActionListener(e -> {
                if (!setting.containsCode(code)) {
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
                }
                operation.setEnabled(false);
            });
            row.add(operation);
            inner.add(row);
        }
        container.setViewportView(inner);
    }
}
