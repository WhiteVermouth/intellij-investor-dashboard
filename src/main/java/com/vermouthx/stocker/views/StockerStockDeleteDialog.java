package com.vermouthx.stocker.views;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class StockerStockDeleteDialog extends DialogWrapper {
    private JPanel mPane;
    private JTextField tfStockCode;
    private JLabel lbStockCode;
    private JLabel lbSample;
    private JLabel lbNote;

    public StockerStockDeleteDialog(String mkType) {
        super(true);
        init();
        setTitle("Delete " + mkType + " Stock Code");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return mPane;
    }

    public List<String> getInput() {
        String text = tfStockCode.getText();
        return Arrays.asList(text.split(","));
    }
}
