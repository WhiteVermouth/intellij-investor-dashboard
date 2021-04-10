package com.vermouthx.stocker.views;

import com.vermouthx.stocker.enums.StockerQuoteColorPattern;
import com.vermouthx.stocker.enums.StockerQuoteProvider;

import javax.swing.*;

public class StockerSettingView {
    private JPanel mPane;
    private JRadioButton rbRedUpPattern;
    private JRadioButton rbGreenUpPattern;
    private JRadioButton rbNonePattern;
    private JComboBox<String> cbProvider;

    public JPanel getContent() {
        for (StockerQuoteProvider value : StockerQuoteProvider.values()) {
            cbProvider.addItem(value.getTitle());
        }
        return mPane;
    }

    public StockerQuoteProvider getSelectedQuoteProvider() {
        return StockerQuoteProvider.SINA;
    }

    public StockerQuoteColorPattern getSelectedQuoteColorPattern() {
        if (rbRedUpPattern.isSelected()) {
            return StockerQuoteColorPattern.RED_UP_GREEN_DOWN;
        }
        if (rbGreenUpPattern.isSelected()) {
            return StockerQuoteColorPattern.GREEN_UP_RED_DOWN;
        }
        return StockerQuoteColorPattern.NONE;
    }

    public void resetQuoteProvider(StockerQuoteProvider quoteProvider) {
        cbProvider.setSelectedItem(quoteProvider.getTitle());
    }

    public void resetQuoteColorPattern(StockerQuoteColorPattern colorPattern) {
        switch (colorPattern) {
            case RED_UP_GREEN_DOWN:
                rbRedUpPattern.setSelected(true);
                break;
            case GREEN_UP_RED_DOWN:
                rbGreenUpPattern.setSelected(true);
                break;
            default:
                rbNonePattern.setSelected(true);
        }
    }

}
