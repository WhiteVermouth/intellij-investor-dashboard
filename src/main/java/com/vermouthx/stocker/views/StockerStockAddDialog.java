package com.vermouthx.stocker.views;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class StockerStockAddDialog extends DialogWrapper {
    private JPanel mPane;
    private JLabel lbStockCode;
    private JTextField tfStockCode;
    private JLabel lbNote;
    private JLabel lbStockCodeSample;

    public StockerStockAddDialog(String mkType) {
        super(true);
        init();
        setTitle("Add " + mkType + " Stock Code");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return mPane;
    }

    public String getInput() {
        return tfStockCode.getText();
    }
}
