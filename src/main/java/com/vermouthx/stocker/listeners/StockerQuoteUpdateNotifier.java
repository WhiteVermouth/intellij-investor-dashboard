package com.vermouthx.stocker.listeners;

import com.intellij.util.messages.Topic;
import com.vermouthx.stocker.entities.StockerQuote;

import java.util.List;

public interface StockerQuoteUpdateNotifier {
    Topic<StockerQuoteUpdateNotifier> STOCK_CN_QUOTE_UPDATE_TOPIC = Topic.create("StockCNQuoteUpdateTopic", StockerQuoteUpdateNotifier.class);
    Topic<StockerQuoteUpdateNotifier> STOCK_HK_QUOTE_UPDATE_TOPIC = Topic.create("StockHKQuoteUpdateTopic", StockerQuoteUpdateNotifier.class);
    Topic<StockerQuoteUpdateNotifier> STOCK_US_QUOTE_UPDATE_TOPIC = Topic.create("StockUSQuoteUpdateTopic", StockerQuoteUpdateNotifier.class);

    void after(List<StockerQuote> quotes);
}
