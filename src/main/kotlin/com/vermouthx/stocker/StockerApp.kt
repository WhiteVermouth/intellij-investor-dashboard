package com.vermouthx.stocker

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.util.Disposer
import com.vermouthx.stocker.entities.StockerQuote
import com.vermouthx.stocker.enums.StockerMarketIndex
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.enums.StockerQuoteProvider
import com.vermouthx.stocker.listeners.StockerQuoteReloadNotifier.*
import com.vermouthx.stocker.listeners.StockerQuoteUpdateNotifier.*
import com.vermouthx.stocker.settings.StockerSetting
import com.vermouthx.stocker.utils.StockerQuoteHttpUtil
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Application-wide quote refresher. The quote topics live on the application message bus, so a
 * single scheduler feeds the tool windows of every open project. It only runs while at least one
 * Stocker tool window is open (see [register]).
 */
@Service(Service.Level.APP)
class StockerApp : Disposable {

    companion object {
        val instance: StockerApp
            get() = ApplicationManager.getApplication().getService(StockerApp::class.java)
    }

    private val setting = StockerSetting.instance
    private val messageBus = ApplicationManager.getApplication().messageBus

    private var scheduledExecutorService: ScheduledExecutorService? = null

    private var scheduleInitialDelay: Long = 3
    @Volatile
    private var refreshActive: Boolean = false

    // Number of open Stocker tool windows across all projects.
    private var viewerCount = 0

    /**
     * Registers an open tool window. Refreshing starts with the first one and stops once
     * [parentDisposable] of the last one is disposed (tool window or project closed).
     */
    @Synchronized
    fun register(parentDisposable: Disposable) {
        viewerCount++
        Disposer.register(parentDisposable) { unregister() }
        if (viewerCount == 1) {
            schedule()
        }
    }

    @Synchronized
    private fun unregister() {
        viewerCount--
        if (viewerCount == 0) {
            shutdown()
        }
    }

    /**
     * (Re)starts refreshing. A no-op while no tool window is open, so callers can always pair it
     * with [shutdownThenClear] after changing the watchlist.
     */
    @Synchronized
    fun schedule() {
        if (viewerCount == 0 || !isShutdown()) {
            return
        }
        val executor = Executors.newScheduledThreadPool(1)
        scheduledExecutorService = executor
        refreshActive = true
        // Use single consolidated task instead of multiple overlapping tasks
        // This reduces HTTP requests by 50% and prevents redundant data fetching
        executor.scheduleAtFixedRate(
            createConsolidatedUpdateThread(),
            scheduleInitialDelay,
            setting.refreshInterval,
            TimeUnit.SECONDS
        )
        scheduleInitialDelay = 0
    }

    @Synchronized
    fun shutdown() {
        refreshActive = false
        scheduledExecutorService?.shutdownNow()
        scheduledExecutorService = null
        StockerQuoteHttpUtil.closeConnections()
    }

    fun isShutdown(): Boolean {
        return scheduledExecutorService == null
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

    override fun dispose() {
        shutdown()
    }

    /**
     * Consolidated update thread that fetches all market data once and publishes to all relevant topics.
     * This eliminates redundant HTTP requests that were previously made by separate per-market tasks.
     */
    private fun createConsolidatedUpdateThread(): Runnable {
        return Runnable {
            if (!shouldContinueRefresh()) {
                return@Runnable
            }

            val quoteProvider = setting.quoteProvider
            val cryptoQuoteProvider = setting.cryptoQuoteProvider

            // Fetch all market data once
            val aShareQuotes = fetchQuotesIfActive(StockerMarketType.AShare, quoteProvider, setting.aShareList) ?: return@Runnable
            val hkStocksQuotes = fetchQuotesIfActive(StockerMarketType.HKStocks, quoteProvider, setting.hkStocksList) ?: return@Runnable
            val usStocksQuotes = fetchQuotesIfActive(StockerMarketType.USStocks, quoteProvider, setting.usStocksList) ?: return@Runnable
            val cryptoQuotes = fetchQuotesIfActive(StockerMarketType.Crypto, cryptoQuoteProvider, setting.cryptoList) ?: return@Runnable

            val aShareIndices = fetchQuotesIfActive(StockerMarketType.AShare, quoteProvider, StockerMarketIndex.CN.codes) ?: return@Runnable
            val hkStocksIndices = fetchQuotesIfActive(StockerMarketType.HKStocks, quoteProvider, StockerMarketIndex.HK.codes) ?: return@Runnable
            val usStocksIndices = fetchQuotesIfActive(StockerMarketType.USStocks, quoteProvider, StockerMarketIndex.US.codes) ?: return@Runnable
            val cryptoIndices = fetchQuotesIfActive(StockerMarketType.Crypto, cryptoQuoteProvider, StockerMarketIndex.Crypto.codes) ?: return@Runnable

            if (!shouldContinueRefresh()) {
                return@Runnable
            }

            // Publish to individual market topics
            // Always publish indices, but only publish quotes when there are favorites
            val cnPublisher = messageBus.syncPublisher(STOCK_CN_QUOTE_UPDATE_TOPIC)
            if (setting.aShareList.isNotEmpty()) {
                cnPublisher.syncQuotes(aShareQuotes)
            }
            cnPublisher.syncIndices(aShareIndices)

            val hkPublisher = messageBus.syncPublisher(STOCK_HK_QUOTE_UPDATE_TOPIC)
            if (setting.hkStocksList.isNotEmpty()) {
                hkPublisher.syncQuotes(hkStocksQuotes)
            }
            hkPublisher.syncIndices(hkStocksIndices)

            val usPublisher = messageBus.syncPublisher(STOCK_US_QUOTE_UPDATE_TOPIC)
            if (setting.usStocksList.isNotEmpty()) {
                usPublisher.syncQuotes(usStocksQuotes)
            }
            usPublisher.syncIndices(usStocksIndices)

            val cryptoPublisher = messageBus.syncPublisher(CRYPTO_QUOTE_UPDATE_TOPIC)
            if (setting.cryptoList.isNotEmpty()) {
                cryptoPublisher.syncQuotes(cryptoQuotes)
            }
            cryptoPublisher.syncIndices(cryptoIndices)

            // Publish to "all" topic
            val allStockQuotes = listOf(aShareQuotes, hkStocksQuotes, usStocksQuotes, cryptoQuotes).flatten()
            val allStockIndices = listOf(aShareIndices, hkStocksIndices, usStocksIndices, cryptoIndices).flatten()
            val allPublisher = messageBus.syncPublisher(STOCK_ALL_QUOTE_UPDATE_TOPIC)
            allPublisher.syncQuotes(allStockQuotes)
            allPublisher.syncIndices(allStockIndices)
        }
    }

    private fun fetchQuotesIfActive(
        marketType: StockerMarketType,
        quoteProvider: StockerQuoteProvider,
        codes: List<String>
    ): List<StockerQuote>? {
        if (!shouldContinueRefresh()) {
            return null
        }
        return StockerQuoteHttpUtil.get(marketType, quoteProvider, codes)
    }

    private fun shouldContinueRefresh(): Boolean {
        return refreshActive && !Thread.currentThread().isInterrupted
    }

}
