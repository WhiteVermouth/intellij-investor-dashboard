package com.vermouthx.stocker.listeners;

import com.intellij.util.messages.Topic;

public interface StockerQuoteDeleteNotifier {
    Topic<StockerQuoteDeleteNotifier> STOCK_ALL_QUOTE_DELETE_TOPIC = Topic.create("StockAllQuoteDeleteTopic", StockerQuoteDeleteNotifier.class);
    Topic<StockerQuoteDeleteNotifier> STOCK_CN_QUOTE_DELETE_TOPIC = Topic.create("StockCNQuoteDeleteTopic", StockerQuoteDeleteNotifier.class);
    Topic<StockerQuoteDeleteNotifier> STOCK_HK_QUOTE_DELETE_TOPIC = Topic.create("StockHKQuoteDeleteTopic", StockerQuoteDeleteNotifier.class);
    Topic<StockerQuoteDeleteNotifier> STOCK_US_QUOTE_DELETE_TOPIC = Topic.create("StockUSQuoteDeleteTopic", StockerQuoteDeleteNotifier.class);
    Topic<StockerQuoteDeleteNotifier> CRYPTO_QUOTE_DELETE_TOPIC = Topic.create("CryptoQuoteDeleteTopic", StockerQuoteDeleteNotifier.class);

    void after(String code);
}
