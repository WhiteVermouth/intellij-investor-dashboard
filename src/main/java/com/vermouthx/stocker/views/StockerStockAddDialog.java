package com.vermouthx.stocker.views;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class StockerStockAddDialog extends DialogWrapper {
    private JTextField tfStockCode;
    private JLabel lbStockCode;
    private JPanel mPane;

    protected StockerStockAddDialog() {
        super(true);
        init();
        setTitle("Add Stock Code");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return mPane;
    }
}
