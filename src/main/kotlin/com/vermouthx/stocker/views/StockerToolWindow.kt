package com.vermouthx.stocker.views

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.enums.StockerQuoteProvider
import com.vermouthx.stocker.listeners.StockerQuoteListener
import com.vermouthx.stocker.listeners.StockerQuoteUpdateNotifier
import com.vermouthx.stocker.notifications.StockerNotification
import com.vermouthx.stocker.settings.StockerSetting
import com.vermouthx.stocker.utils.StockerQuoteHttpUtil
import kotlin.concurrent.thread

class StockerToolWindow : ToolWindowFactory {

    companion object {
        private val setting = StockerSetting.instance
        private val messageBus = ApplicationManager.getApplication().messageBus
    }

    private lateinit var tabViewMap: Map<StockerMarketType, StockerUIView>
    private lateinit var aShareThread: Thread
    private lateinit var hkStocksThread: Thread
    private lateinit var usStocksThread: Thread

    override fun init(toolWindow: ToolWindow) {
        super.init(toolWindow)
        tabViewMap = mapOf(
                StockerMarketType.AShare to StockerUIView(),
                StockerMarketType.HKStocks to StockerUIView(),
                StockerMarketType.USStocks to StockerUIView()
        )
        aShareThread = createQuoteUpdateThread(
                StockerMarketType.AShare,
                setting.quoteProvider,
                setting.aShareList
        )
        hkStocksThread = createQuoteUpdateThread(
                StockerMarketType.HKStocks,
                setting.quoteProvider,
                setting.hkStocksList
        )
        usStocksThread = createQuoteUpdateThread(
                StockerMarketType.USStocks,
                setting.quoteProvider,
                setting.usStocksList
        )
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentManager = toolWindow.contentManager
        val contentFactory = ContentFactory.SERVICE.getInstance()
        setupActionListener(project)
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
    }

    private fun setupActionListener(project: Project) {
        tabViewMap.forEach { (k, v) ->
            v.btnAdd.addActionListener {
                val dialog = StockerStockAddDialog(k.title)
                if (dialog.showAndGet()) {
                    val input = dialog.input
                    input.split(",").forEach {
                        if (StockerQuoteHttpUtil.validateCode(k, setting.quoteProvider, it)) {
                            val setting = StockerSetting.instance
                            when (k) {
                                StockerMarketType.AShare -> {
                                    if (!setting.aShareList.contains(it.toUpperCase())) {
                                        setting.aShareList.add(it.toUpperCase())
                                    }
                                }
                                StockerMarketType.HKStocks -> {
                                    if (!setting.hkStocksList.contains(it.toUpperCase())) {
                                        setting.hkStocksList.add(it.toUpperCase())
                                    }
                                }
                                StockerMarketType.USStocks -> {
                                    if (!setting.usStocksList.contains(it.toUpperCase())) {
                                        setting.usStocksList.add(it.toUpperCase())
                                    }
                                }
                            }
                        } else {
                            StockerNotification.notifyInvalidCode(project, it)
                        }
                    }
                }
            }
            v.btnDelete.addActionListener {
                val dialog = StockerStockDeleteDialog(k.title)
                if (dialog.showAndGet()) {
                    dialog.input.map { it.toUpperCase() }
                            .forEach {
                                setting.removeCode(k, it)
                            }
                    v.tbModel.dataVector.clear()
                    v.tbModel.fireTableDataChanged()
                }
            }
            v.btnRefresh.addActionListener {
                if (!aShareThread.isAlive) {
                    aShareThread = createQuoteUpdateThread(StockerMarketType.AShare, setting.quoteProvider, setting.aShareList)
                }
                if (!hkStocksThread.isAlive) {
                    hkStocksThread = createQuoteUpdateThread(StockerMarketType.HKStocks, setting.quoteProvider, setting.hkStocksList)
                }
                if (!usStocksThread.isAlive) {
                    usStocksThread = createQuoteUpdateThread(StockerMarketType.USStocks, setting.quoteProvider, setting.usStocksList)
                }
            }
            when (k) {
                StockerMarketType.AShare -> {
                    messageBus.connect()
                            .subscribe(
                                    StockerQuoteUpdateNotifier.STOCK_CN_QUOTE_UPDATE_TOPIC,
                                    StockerQuoteListener(v.tbModel)
                            )
                }
                StockerMarketType.HKStocks -> {
                    messageBus.connect()
                            .subscribe(
                                    StockerQuoteUpdateNotifier.STOCK_HK_QUOTE_UPDATE_TOPIC,
                                    StockerQuoteListener(v.tbModel)
                            )
                }
                StockerMarketType.USStocks -> {
                    messageBus.connect()
                            .subscribe(
                                    StockerQuoteUpdateNotifier.STOCK_US_QUOTE_UPDATE_TOPIC,
                                    StockerQuoteListener(v.tbModel)
                            )

                }
            }
        }
    }

    private fun createQuoteUpdateThread(
            marketType: StockerMarketType,
            quoteProvider: StockerQuoteProvider,
            stockCodeList: List<String>
    ): Thread {
        return thread(start = true) {
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