package com.vermouthx.stocker

import com.intellij.openapi.application.ApplicationManager
import com.vermouthx.stocker.enums.StockerMarketIndex
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.listeners.StockerQuoteUpdateNotifier
import com.vermouthx.stocker.settings.StockerSetting
import com.vermouthx.stocker.utils.StockerQuoteHttpUtil
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

object StockerApp {

    const val pluginId = "com.vermouthx.intellij-investor-dashboard"

    private val setting = StockerSetting.instance
    private val messageBus = ApplicationManager.getApplication().messageBus

    private val scheduledExecutorService: ScheduledExecutorService = Executors.newScheduledThreadPool(3)

    private const val scheduleInitialDelay: Long = 3
    private const val schedulePeriod: Long = 2

    fun schedule() {
        scheduledExecutorService.scheduleAtFixedRate(
            createAllQuoteUpdateThread(StockerMarketType.AShare, setting.aShareList),
            scheduleInitialDelay, schedulePeriod, TimeUnit.SECONDS
        )
        scheduledExecutorService.scheduleAtFixedRate(
            createAllQuoteUpdateThread(StockerMarketType.HKStocks, setting.hkStocksList),
            scheduleInitialDelay, schedulePeriod, TimeUnit.SECONDS
        )
        scheduledExecutorService.scheduleAtFixedRate(
            createAllQuoteUpdateThread(StockerMarketType.USStocks, setting.usStocksList),
            scheduleInitialDelay, schedulePeriod, TimeUnit.SECONDS
        )
        scheduledExecutorService.scheduleAtFixedRate(
            createAllQuoteUpdateThread(),
            scheduleInitialDelay, schedulePeriod, TimeUnit.SECONDS
        )
    }

    fun refresh() {
        refresh(StockerMarketType.AShare, setting.aShareList)
        refresh(StockerMarketType.HKStocks, setting.hkStocksList)
        refresh(StockerMarketType.USStocks, setting.usStocksList)
    }

    private fun createAllQuoteUpdateThread(): Runnable {
        return Runnable {
            val quoteProvider = setting.quoteProvider
            val allStockQuotes = listOf(
                StockerQuoteHttpUtil.get(StockerMarketType.AShare, quoteProvider, setting.aShareList),
                StockerQuoteHttpUtil.get(StockerMarketType.HKStocks, quoteProvider, setting.hkStocksList),
                StockerQuoteHttpUtil.get(StockerMarketType.USStocks, quoteProvider, setting.usStocksList)
            ).flatten()
            val allStockIndices = listOf(
                StockerQuoteHttpUtil.get(StockerMarketType.AShare, quoteProvider, StockerMarketIndex.CN.codes),
                StockerQuoteHttpUtil.get(StockerMarketType.HKStocks, quoteProvider, StockerMarketIndex.HK.codes),
                StockerQuoteHttpUtil.get(StockerMarketType.USStocks, quoteProvider, StockerMarketIndex.US.codes)
            ).flatten()
            val publisher = messageBus.syncPublisher(StockerQuoteUpdateNotifier.STOCK_ALL_QUOTE_UPDATE_TOPIC)
            publisher.syncQuotes(allStockQuotes)
            publisher.syncIndices(allStockIndices)
        }
    }

    private fun createAllQuoteUpdateThread(marketType: StockerMarketType, stockCodeList: List<String>): Runnable {
        return Runnable {
            refresh(marketType, stockCodeList)
        }
    }

    private fun refresh(
        marketType: StockerMarketType,
        stockCodeList: List<String>
    ) {
        val quoteProvider = setting.quoteProvider
        when (marketType) {
            StockerMarketType.AShare -> {
                val quotes = StockerQuoteHttpUtil.get(marketType, quoteProvider, stockCodeList)
                val indices = StockerQuoteHttpUtil.get(marketType, quoteProvider, StockerMarketIndex.CN.codes)
                val publisher =
                    messageBus.syncPublisher(StockerQuoteUpdateNotifier.STOCK_CN_QUOTE_UPDATE_TOPIC)
                publisher.syncQuotes(quotes)
                publisher.syncIndices(indices)
            }
            StockerMarketType.HKStocks -> {
                val quotes = StockerQuoteHttpUtil.get(marketType, quoteProvider, stockCodeList)
                val indices = StockerQuoteHttpUtil.get(marketType, quoteProvider, StockerMarketIndex.HK.codes)
                val publisher =
                    messageBus.syncPublisher(StockerQuoteUpdateNotifier.STOCK_HK_QUOTE_UPDATE_TOPIC)
                publisher.syncQuotes(quotes)
                publisher.syncIndices(indices)
            }
            StockerMarketType.USStocks -> {
                val quotes = StockerQuoteHttpUtil.get(marketType, quoteProvider, stockCodeList)
                val indices = StockerQuoteHttpUtil.get(marketType, quoteProvider, StockerMarketIndex.US.codes)
                val publisher =
                    messageBus.syncPublisher(StockerQuoteUpdateNotifier.STOCK_US_QUOTE_UPDATE_TOPIC)
                publisher.syncQuotes(quotes)
                publisher.syncIndices(indices)
            }
        }
    }


}