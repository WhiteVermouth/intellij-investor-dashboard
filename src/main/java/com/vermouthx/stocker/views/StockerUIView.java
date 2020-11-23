package com.vermouthx.stocker.views;

import javax.swing.*;

public class StockerUIView {
    private JPanel mPane;
    private JScrollPane sPane;
    private JTable tbView;
    private JButton btnAdd;
    private JButton btnRefresh;

    public JPanel getContent() {
        return mPane;
    }

    public JTable getTbView() {
        return tbView;
    }

    public JButton getBtnAdd() {
        return btnAdd;
    }

    public JButton getBtnRefresh() {
        return btnRefresh;
    }
}
