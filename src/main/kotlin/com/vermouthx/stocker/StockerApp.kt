package com.vermouthx.stocker

import com.intellij.openapi.application.ApplicationManager
import com.vermouthx.stocker.enums.StockerMarketIndex
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.listeners.StockerQuoteReloadNotifier.*
import com.vermouthx.stocker.listeners.StockerQuoteUpdateNotifier.*
import com.vermouthx.stocker.settings.StockerSetting
import com.vermouthx.stocker.utils.StockerQuoteHttpUtil
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class StockerApp {

    private val setting = StockerSetting.instance
    private val messageBus = ApplicationManager.getApplication().messageBus

    private var scheduledExecutorService: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    private var scheduleInitialDelay: Long = 3
    private val schedulePeriod: Long = StockerSetting.instance.refreshInterval

    fun schedule() {
        if (scheduledExecutorService.isShutdown) {
            scheduledExecutorService = Executors.newScheduledThreadPool(1)
            scheduleInitialDelay = 0
        }
        // Use single consolidated task instead of multiple overlapping tasks
        // This reduces HTTP requests by 50% and prevents redundant data fetching
        scheduledExecutorService.scheduleAtFixedRate(
            createConsolidatedUpdateThread(),
            scheduleInitialDelay,
            schedulePeriod,
            TimeUnit.SECONDS
        )
    }

    fun shutdown() {
        scheduledExecutorService.shutdown()
    }

    fun isShutdown(): Boolean {
        return scheduledExecutorService.isShutdown
    }

    private fun clear() {
        messageBus.syncPublisher(STOCK_ALL_QUOTE_RELOAD_TOPIC).clear()
        messageBus.syncPublisher(STOCK_CN_QUOTE_RELOAD_TOPIC).clear()
        messageBus.syncPublisher(STOCK_HK_QUOTE_RELOAD_TOPIC).clear()
        messageBus.syncPublisher(STOCK_US_QUOTE_RELOAD_TOPIC).clear()
        messageBus.syncPublisher(STOCK_CRYPTO_QUOTE_RELOAD_TOPIC).clear()
    }

    fun shutdownThenClear() {
        shutdown()
        clear()
    }

    /**
     * Consolidated update thread that fetches all market data once and publishes to all relevant topics.
     * This eliminates redundant HTTP requests that were previously made by separate per-market tasks.
     */
    private fun createConsolidatedUpdateThread(): Runnable {
        return Runnable {
            val quoteProvider = setting.quoteProvider
            val cryptoQuoteProvider = setting.cryptoQuoteProvider
            
            // Fetch all market data once
            val aShareQuotes = StockerQuoteHttpUtil.get(StockerMarketType.AShare, quoteProvider, setting.aShareList)
            val hkStocksQuotes = StockerQuoteHttpUtil.get(StockerMarketType.HKStocks, quoteProvider, setting.hkStocksList)
            val usStocksQuotes = StockerQuoteHttpUtil.get(StockerMarketType.USStocks, quoteProvider, setting.usStocksList)
            val cryptoQuotes = StockerQuoteHttpUtil.get(StockerMarketType.Crypto, cryptoQuoteProvider, setting.cryptoList)
            
            val aShareIndices = StockerQuoteHttpUtil.get(StockerMarketType.AShare, quoteProvider, StockerMarketIndex.CN.codes)
            val hkStocksIndices = StockerQuoteHttpUtil.get(StockerMarketType.HKStocks, quoteProvider, StockerMarketIndex.HK.codes)
            val usStocksIndices = StockerQuoteHttpUtil.get(StockerMarketType.USStocks, quoteProvider, StockerMarketIndex.US.codes)
            val cryptoIndices = StockerQuoteHttpUtil.get(StockerMarketType.Crypto, cryptoQuoteProvider, StockerMarketIndex.Crypto.codes)
            
            // Publish to individual market topics
            if (setting.aShareList.isNotEmpty()) {
                val publisher = messageBus.syncPublisher(STOCK_CN_QUOTE_UPDATE_TOPIC)
                publisher.syncQuotes(aShareQuotes, setting.aShareList.size)
                publisher.syncIndices(aShareIndices)
            }
            
            if (setting.hkStocksList.isNotEmpty()) {
                val publisher = messageBus.syncPublisher(STOCK_HK_QUOTE_UPDATE_TOPIC)
                publisher.syncQuotes(hkStocksQuotes, setting.hkStocksList.size)
                publisher.syncIndices(hkStocksIndices)
            }
            
            if (setting.usStocksList.isNotEmpty()) {
                val publisher = messageBus.syncPublisher(STOCK_US_QUOTE_UPDATE_TOPIC)
                publisher.syncQuotes(usStocksQuotes, setting.usStocksList.size)
                publisher.syncIndices(usStocksIndices)
            }
            
            if (setting.cryptoList.isNotEmpty()) {
                val publisher = messageBus.syncPublisher(CRYPTO_QUOTE_UPDATE_TOPIC)
                publisher.syncQuotes(cryptoQuotes, setting.cryptoList.size)
                publisher.syncIndices(cryptoIndices)
            }
            
            // Publish to "all" topic
            val allStockQuotes = listOf(aShareQuotes, hkStocksQuotes, usStocksQuotes, cryptoQuotes).flatten()
            val allStockIndices = listOf(aShareIndices, hkStocksIndices, usStocksIndices, cryptoIndices).flatten()
            val allPublisher = messageBus.syncPublisher(STOCK_ALL_QUOTE_UPDATE_TOPIC)
            allPublisher.syncQuotes(allStockQuotes, setting.allStockListSize)
            allPublisher.syncIndices(allStockIndices)
        }
    }

}
