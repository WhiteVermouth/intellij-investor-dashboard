package com.vermouthx.stocker

import com.intellij.openapi.application.ApplicationManager
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.listeners.StockerQuoteUpdateNotifier
import com.vermouthx.stocker.settings.StockerSetting
import com.vermouthx.stocker.utils.StockerQuoteHttpUtil
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

object StockerApp {

    const val pluginId = "com.vermouthx.intellij-investor-dashboard"

    private val setting = StockerSetting.instance
    private val messageBus = ApplicationManager.getApplication().messageBus

    private val scheduledExecutorService: ScheduledExecutorService = Executors.newScheduledThreadPool(3)

    fun schedule() {
        scheduledExecutorService.scheduleAtFixedRate(
            createQuoteUpdateThread(StockerMarketType.AShare, setting.aShareList),
            0, 1, TimeUnit.SECONDS
        )
        scheduledExecutorService.scheduleAtFixedRate(
            createQuoteUpdateThread(StockerMarketType.HKStocks, setting.hkStocksList),
            0, 1, TimeUnit.SECONDS
        )
        scheduledExecutorService.scheduleAtFixedRate(
            createQuoteUpdateThread(StockerMarketType.USStocks, setting.usStocksList),
            0, 1, TimeUnit.SECONDS
        )
    }

    fun refresh() {
        refresh(StockerMarketType.AShare, setting.aShareList)
        refresh(StockerMarketType.HKStocks, setting.hkStocksList)
        refresh(StockerMarketType.USStocks, setting.usStocksList)
    }

    private fun createQuoteUpdateThread(marketType: StockerMarketType, stockCodeList: List<String>): Thread {
        return thread {
            refresh(marketType, stockCodeList)
        }
    }

    private fun refresh(
        marketType: StockerMarketType,
        stockCodeList: List<String>
    ) {
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
    }


}