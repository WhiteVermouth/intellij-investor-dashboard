package com.vermouthx.stocker.views;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBPanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class StockerStockAddDialog extends DialogWrapper {
    private JPanel mPane;

    public StockerStockAddDialog(Project project) {
        super(project, true);
        init();
        setTitle("Add Stock Code");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        mPane = new JBPanel<>();
        return mPane;
    }
}
