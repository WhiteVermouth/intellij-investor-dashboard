package com.vermouthx.stocker.components;

import javax.swing.table.DefaultTableModel;

public class StockerTableModel extends DefaultTableModel {

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
