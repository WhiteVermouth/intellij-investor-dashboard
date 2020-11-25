package com.vermouthx.stocker.views;

import com.vermouthx.stocker.enums.StockerQuoteColorPattern;
import com.vermouthx.stocker.enums.StockerQuoteProvider;

import javax.swing.*;
import java.util.Objects;

public class StockerSettingView {
    private JPanel mPane;
    private JRadioButton rbRedUpPattern;
    private JRadioButton rbGreenUpPattern;
    private JLabel lbProvider;
    private JLabel lbColorPattern;
    private JComboBox<String> cbProvider;

    public JPanel getContent() {
        cbProvider.addItem(StockerQuoteProvider.SINA.getTitle());
        cbProvider.setSelectedIndex(0);
        return mPane;
    }

    public StockerQuoteProvider getSelectedProvider() {
        String title = Objects.requireNonNull(cbProvider.getSelectedItem()).toString();
        switch (title) {
            case "Sina":
                return StockerQuoteProvider.SINA;
            case "Tencent":
                return StockerQuoteProvider.TENCENT;
        }
        return null;
    }

    public StockerQuoteColorPattern getColorPattern() {
        if (rbRedUpPattern.isSelected()) {
            return StockerQuoteColorPattern.RED_UP_GREEN_DOWN;
        }
        return StockerQuoteColorPattern.GREEN_UP_RED_DOWN;
    }

    public void resetColorPattern(StockerQuoteColorPattern colorPattern) {
        if (colorPattern == StockerQuoteColorPattern.RED_UP_GREEN_DOWN) {
            rbRedUpPattern.setSelected(true);
        } else {
            rbGreenUpPattern.setSelected(true);
        }
    }
}
