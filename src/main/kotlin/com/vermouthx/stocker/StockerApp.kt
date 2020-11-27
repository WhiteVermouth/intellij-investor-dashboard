package com.vermouthx.stocker

import com.intellij.openapi.application.ApplicationManager
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.listeners.StockerQuoteUpdateNotifier
import com.vermouthx.stocker.settings.StockerSetting
import com.vermouthx.stocker.utils.StockerQuoteHttpUtil
import kotlin.concurrent.thread

object StockerApp {

    const val pluginId = "com.vermouthx.intellij-investor-dashboard"

    private val setting = StockerSetting.instance
    private val messageBus = ApplicationManager.getApplication().messageBus

    private var aShareThread: Thread
    private var hkStocksThread: Thread
    private var usStocksThread: Thread

    init {
        aShareThread = createQuoteUpdateThread(StockerMarketType.AShare, setting.aShareList)
        hkStocksThread = createQuoteUpdateThread(StockerMarketType.HKStocks, setting.hkStocksList)
        usStocksThread = createQuoteUpdateThread(StockerMarketType.USStocks, setting.usStocksList)
    }

    fun reload() {
        if (!aShareThread.isAlive) {
            aShareThread = createQuoteUpdateThread(StockerMarketType.AShare, setting.aShareList)
        }
        if (!hkStocksThread.isAlive) {
            hkStocksThread = createQuoteUpdateThread(StockerMarketType.HKStocks, setting.hkStocksList)
        }
        if (!usStocksThread.isAlive) {
            usStocksThread = createQuoteUpdateThread(StockerMarketType.USStocks, setting.usStocksList)
        }
    }

    private fun createQuoteUpdateThread(marketType: StockerMarketType, stockCodeList: List<String>): Thread {
        return thread(start = true) {
            while (true) {
                val quotes = if (stockCodeList.isNotEmpty()) {
                    StockerQuoteHttpUtil.get(marketType, setting.quoteProvider, stockCodeList)
                } else {
                    emptyList()
                }
                when (marketType) {
                    StockerMarketType.AShare -> {
                        val publisher =
                            messageBus.syncPublisher(StockerQuoteUpdateNotifier.STOCK_CN_QUOTE_UPDATE_TOPIC)
                        publisher.after(quotes)
                    }
                    StockerMarketType.HKStocks -> {
                        val publisher =
                            messageBus.syncPublisher(StockerQuoteUpdateNotifier.STOCK_HK_QUOTE_UPDATE_TOPIC)
                        publisher.after(quotes)
                    }
                    StockerMarketType.USStocks -> {
                        val publisher =
                            messageBus.syncPublisher(StockerQuoteUpdateNotifier.STOCK_US_QUOTE_UPDATE_TOPIC)
                        publisher.after(quotes)
                    }
                }
                Thread.sleep(setting.refreshInterval)
            }
        }
    }


}