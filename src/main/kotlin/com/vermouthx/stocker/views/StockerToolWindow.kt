package com.vermouthx.stocker.views

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.vermouthx.stocker.StockerApp
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.listeners.StockerQuoteDeleteListener
import com.vermouthx.stocker.listeners.StockerQuoteDeleteNotifier
import com.vermouthx.stocker.listeners.StockerQuoteUpdateListener
import com.vermouthx.stocker.listeners.StockerQuoteUpdateNotifier

class StockerToolWindow : ToolWindowFactory {

    companion object {
        private val messageBus = ApplicationManager.getApplication().messageBus
    }

    private lateinit var allView: StockerSimpleToolWindow
    private lateinit var tabViewMap: Map<StockerMarketType, StockerSimpleToolWindow>

    override fun init(toolWindow: ToolWindow) {
        super.init(toolWindow)
        allView = StockerSimpleToolWindow()
        tabViewMap = mapOf(
            StockerMarketType.AShare to StockerSimpleToolWindow(),
            StockerMarketType.HKStocks to StockerSimpleToolWindow(),
            StockerMarketType.USStocks to StockerSimpleToolWindow()
        )
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentManager = toolWindow.contentManager
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val allContent = contentFactory.createContent(allView.component, "ALL", false)
        contentManager.addContent(allContent)
        val aShareContent = contentFactory.createContent(
            tabViewMap[StockerMarketType.AShare]?.component,
            StockerMarketType.AShare.title,
            false
        )
        contentManager.addContent(aShareContent)
        val hkStocksContent = contentFactory.createContent(
            tabViewMap[StockerMarketType.HKStocks]?.component,
            StockerMarketType.HKStocks.title,
            false
        )
        contentManager.addContent(hkStocksContent)
        val usStocksContent = contentFactory.createContent(
            tabViewMap[StockerMarketType.USStocks]?.component,
            StockerMarketType.USStocks.title,
            false
        )
        contentManager.addContent(usStocksContent)
        StockerApp.reload()
        this.subscribeMessage()
    }

    private fun subscribeMessage() {
        tabViewMap.forEach { (k, v) ->
            when (k) {
                StockerMarketType.AShare -> {
                    messageBus.connect()
                        .subscribe(
                            StockerQuoteUpdateNotifier.STOCK_CN_QUOTE_UPDATE_TOPIC,
                            StockerQuoteUpdateListener(
                                allView.tableView,
                                v.tableView
                            )
                        )
                    messageBus.connect()
                        .subscribe(
                            StockerQuoteDeleteNotifier.STOCK_CN_QUOTE_DELETE_TOPIC,
                            StockerQuoteDeleteListener(
                                allView.tableView,
                                v.tableView
                            )
                        )
                }
                StockerMarketType.HKStocks -> {
                    messageBus.connect()
                        .subscribe(
                            StockerQuoteUpdateNotifier.STOCK_HK_QUOTE_UPDATE_TOPIC,
                            StockerQuoteUpdateListener(
                                allView.tableView,
                                v.tableView
                            )
                        )
                    messageBus.connect()
                        .subscribe(
                            StockerQuoteDeleteNotifier.STOCK_HK_QUOTE_DELETE_TOPIC,
                            StockerQuoteDeleteListener(
                                allView.tableView,
                                v.tableView
                            )
                        )
                }
                StockerMarketType.USStocks -> {
                    messageBus.connect()
                        .subscribe(
                            StockerQuoteUpdateNotifier.STOCK_US_QUOTE_UPDATE_TOPIC,
                            StockerQuoteUpdateListener(
                                allView.tableView,
                                v.tableView
                            )
                        )
                    messageBus.connect()
                        .subscribe(
                            StockerQuoteDeleteNotifier.STOCK_US_QUOTE_DELETE_TOPIC,
                            StockerQuoteDeleteListener(
                                allView.tableView,
                                v.tableView
                            )
                        )
                }
            }
        }
    }
}