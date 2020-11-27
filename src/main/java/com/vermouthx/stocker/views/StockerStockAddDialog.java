package com.vermouthx.stocker.views;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.vermouthx.stocker.enums.StockerMarketType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class StockerStockAddDialog extends DialogWrapper {
    private JPanel mPane;
    private JTextField tfStockCode;
    private JComboBox<StockerMarketType> cbMarket;

    public StockerStockAddDialog(Project project) {
        super(project, true);
        init();
        setTitle("Add Stock Code");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        for (StockerMarketType mkType : StockerMarketType.values()) {
            cbMarket.addItem(mkType);
        }
        cbMarket.setSelectedIndex(0);
        return mPane;
    }

    public String getInput() {
        return tfStockCode.getText();
    }

    public @NotNull StockerMarketType getMarket() {
        return (StockerMarketType) Objects.requireNonNull(cbMarket.getSelectedItem());
    }
}
