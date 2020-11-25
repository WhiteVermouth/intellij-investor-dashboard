package com.vermouthx.stocker.activities

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.enums.StockerQuoteProvider
import com.vermouthx.stocker.listeners.StockerQuoteUpdateNotifier
import com.vermouthx.stocker.settings.StockerSetting
import com.vermouthx.stocker.utils.StockerQuoteHttpUtil
import com.vermouthx.stocker.views.StockerToolWindow
import kotlin.concurrent.thread

class StockerStartupActivity : StartupActivity {

    companion object {
        private const val pluginId = "com.vermouthx.intellij-investor-dashboard"
        private val messageBus = ApplicationManager.getApplication().messageBus
        private val setting = StockerSetting.instance
    }

    override fun runActivity(project: Project) {
        val currentVersion = PluginManagerCore.getPlugin(PluginId.getId(pluginId))?.version ?: ""
        if (setting.version != currentVersion) {
            setting.version = currentVersion
        }
        createQuoteUpdateThread(
                StockerMarketType.AShare,
                setting.quoteProvider,
                StockerToolWindow.setting.aShareList
        )
        createQuoteUpdateThread(
                StockerMarketType.HKStocks,
                setting.quoteProvider,
                StockerToolWindow.setting.hkStocksList
        )
        createQuoteUpdateThread(
                StockerMarketType.USStocks,
                setting.quoteProvider,
                StockerToolWindow.setting.usStocksList
        )
    }

    private fun createQuoteUpdateThread(
            marketType: StockerMarketType,
            quoteProvider: StockerQuoteProvider,
            stockCodeList: List<String>
    ) {
        thread(start = true) {
            while (true) {
                val quotes = if (stockCodeList.isNotEmpty()) {
                    StockerQuoteHttpUtil.get(marketType, quoteProvider, stockCodeList)
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
                Thread.sleep(1000)
            }
        }
    }

}