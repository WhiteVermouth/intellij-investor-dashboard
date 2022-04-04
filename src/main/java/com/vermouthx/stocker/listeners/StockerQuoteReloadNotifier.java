package com.vermouthx.stocker.listeners;

import com.intellij.util.messages.Topic;

public interface StockerQuoteReloadNotifier {
    Topic<StockerQuoteReloadNotifier> STOCK_ALL_QUOTE_RELOAD_TOPIC = Topic.create("StockerAllQuoteReloadTopic", StockerQuoteReloadNotifier.class);
    Topic<StockerQuoteReloadNotifier> STOCK_CN_QUOTE_RELOAD_TOPIC = Topic.create("StockerCNQuoteReloadTopic", StockerQuoteReloadNotifier.class);
    Topic<StockerQuoteReloadNotifier> STOCK_HK_QUOTE_RELOAD_TOPIC = Topic.create("StockerHKQuoteReloadTopic", StockerQuoteReloadNotifier.class);
    Topic<StockerQuoteReloadNotifier> STOCK_US_QUOTE_RELOAD_TOPIC = Topic.create("StockerUSQuoteReloadTopic", StockerQuoteReloadNotifier.class);
    Topic<StockerQuoteReloadNotifier> STOCK_CRYPTO_QUOTE_RELOAD_TOPIC = Topic.create("StockerCryptoQuoteReloadTopic", StockerQuoteReloadNotifier.class);

    void clear();
}
