package com.vermouthx.stocker.views

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.listeners.StockerQuoteListener
import com.vermouthx.stocker.listeners.StockerQuoteUpdateNotifier
import com.vermouthx.stocker.settings.StockerSetting
import com.vermouthx.stocker.utils.StockerQuoteHttpUtil
import kotlin.concurrent.thread

class StockerToolWindow : ToolWindowFactory {

    companion object {
        private val setting = StockerSetting.instance
        private val messageBus = ApplicationManager.getApplication().messageBus
    }

    private lateinit var tabViewMap: Map<StockerMarketType, StockerSimpleToolWindow>
    private lateinit var aShareThread: Thread
    private lateinit var hkStocksThread: Thread
    private lateinit var usStocksThread: Thread

    override fun init(toolWindow: ToolWindow) {
        super.init(toolWindow)
        tabViewMap = mapOf(
                StockerMarketType.AShare to StockerSimpleToolWindow(StockerTableView()),
                StockerMarketType.HKStocks to StockerSimpleToolWindow(StockerTableView()),
                StockerMarketType.USStocks to StockerSimpleToolWindow(StockerTableView())
        )
        aShareThread = createQuoteUpdateThread(StockerMarketType.AShare, setting.aShareList)
        hkStocksThread = createQuoteUpdateThread(StockerMarketType.HKStocks, setting.hkStocksList)
        usStocksThread = createQuoteUpdateThread(StockerMarketType.USStocks, setting.usStocksList)
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentManager = toolWindow.contentManager
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val aShareContent = contentFactory.createContent(
                tabViewMap[StockerMarketType.AShare]?.content,
                StockerMarketType.AShare.title,
                false
        )
        contentManager.addContent(aShareContent)
        val hkStocksContent = contentFactory.createContent(
                tabViewMap[StockerMarketType.HKStocks]?.content,
                StockerMarketType.HKStocks.title,
                false
        )
        contentManager.addContent(hkStocksContent)
        val usStocksContent = contentFactory.createContent(
                tabViewMap[StockerMarketType.USStocks]?.content,
                StockerMarketType.USStocks.title,
                false
        )
        contentManager.addContent(usStocksContent)
        this.subscribeMessage()
    }

    private fun subscribeMessage() {
        tabViewMap.forEach { (k, v) ->
            when (k) {
                StockerMarketType.AShare -> {
                    messageBus.connect()
                            .subscribe(
                                    StockerQuoteUpdateNotifier.STOCK_CN_QUOTE_UPDATE_TOPIC,
                                    StockerQuoteListener(v.tableView)
                            )
                }
                StockerMarketType.HKStocks -> {
                    messageBus.connect()
                            .subscribe(
                                    StockerQuoteUpdateNotifier.STOCK_HK_QUOTE_UPDATE_TOPIC,
                                    StockerQuoteListener(v.tableView)
                            )
                }
                StockerMarketType.USStocks -> {
                    messageBus.connect()
                            .subscribe(
                                    StockerQuoteUpdateNotifier.STOCK_US_QUOTE_UPDATE_TOPIC,
                                    StockerQuoteListener(v.tableView)
                            )

                }
            }
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