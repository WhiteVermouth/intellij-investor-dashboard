package com.vermouthx.stocker.utils;

import javax.swing.table.DefaultTableModel;

public final class StockerTableModelUtil {
    public static int existAt(DefaultTableModel tableModel, String code) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String c = tableModel.getValueAt(i, 0).toString();
            if (code != null && code.equals(c)) {
                return i;
            }
        }
        return -1;
    }

    private StockerTableModelUtil() {
    }
}
