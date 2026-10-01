package com.vermouthx.stocker.views.windows

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.messages.Topic
import com.vermouthx.stocker.StockerApp
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.listeners.StockerQuoteDeleteListener
import com.vermouthx.stocker.listeners.StockerQuoteDeleteNotifier
import com.vermouthx.stocker.listeners.StockerQuoteDeleteNotifier.*
import com.vermouthx.stocker.listeners.StockerQuoteReloadListener
import com.vermouthx.stocker.listeners.StockerQuoteReloadNotifier
import com.vermouthx.stocker.listeners.StockerQuoteReloadNotifier.*
import com.vermouthx.stocker.listeners.StockerQuoteUpdateListener
import com.vermouthx.stocker.listeners.StockerQuoteUpdateNotifier
import com.vermouthx.stocker.listeners.StockerQuoteUpdateNotifier.*
import com.vermouthx.stocker.views.StockerTableView

/**
 * The platform shares one factory instance across all projects, so everything here is created
 * per call and tied to the tool window's disposable instead of being kept in fields.
 */
class StockerToolWindow : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentManager = toolWindow.contentManager
        val contentFactory = ContentFactory.getInstance()
        val disposable = toolWindow.disposable

        val allView = StockerSimpleToolWindow()
        val tabViewMap = StockerMarketType.entries.associateWith { StockerSimpleToolWindow(it) }

        contentManager.addContent(contentFactory.createContent(allView.component, "ALL", false))
        tabViewMap.forEach { (market, view) ->
            contentManager.addContent(contentFactory.createContent(view.component, market.title, false))
        }

        // Disconnected automatically when the tool window (or its project) is disposed.
        val connection = ApplicationManager.getApplication().messageBus.connect(disposable)
        connection.subscribeTable(
            allView.tableView,
            STOCK_ALL_QUOTE_UPDATE_TOPIC, STOCK_ALL_QUOTE_DELETE_TOPIC, STOCK_ALL_QUOTE_RELOAD_TOPIC
        )
        tabViewMap.forEach { (market, view) ->
            when (market) {
                StockerMarketType.AShare -> connection.subscribeTable(
                    view.tableView,
                    STOCK_CN_QUOTE_UPDATE_TOPIC, STOCK_CN_QUOTE_DELETE_TOPIC, STOCK_CN_QUOTE_RELOAD_TOPIC
                )

                StockerMarketType.HKStocks -> connection.subscribeTable(
                    view.tableView,
                    STOCK_HK_QUOTE_UPDATE_TOPIC, STOCK_HK_QUOTE_DELETE_TOPIC, STOCK_HK_QUOTE_RELOAD_TOPIC
                )

                StockerMarketType.USStocks -> connection.subscribeTable(
                    view.tableView,
                    STOCK_US_QUOTE_UPDATE_TOPIC, STOCK_US_QUOTE_DELETE_TOPIC, STOCK_US_QUOTE_RELOAD_TOPIC
                )

                StockerMarketType.Crypto -> connection.subscribeTable(
                    view.tableView,
                    CRYPTO_QUOTE_UPDATE_TOPIC, CRYPTO_QUOTE_DELETE_TOPIC, STOCK_CRYPTO_QUOTE_RELOAD_TOPIC
                )
            }
        }

        Disposer.register(disposable) {
            allView.tableView.dispose()
            tabViewMap.values.forEach { it.tableView.dispose() }
        }

        StockerApp.instance.register(disposable)
    }

    private fun MessageBusConnection.subscribeTable(
        tableView: StockerTableView,
        updateTopic: Topic<StockerQuoteUpdateNotifier>,
        deleteTopic: Topic<StockerQuoteDeleteNotifier>,
        reloadTopic: Topic<StockerQuoteReloadNotifier>
    ) {
        subscribe(updateTopic, StockerQuoteUpdateListener(tableView))
        subscribe(deleteTopic, StockerQuoteDeleteListener(tableView))
        subscribe(reloadTopic, StockerQuoteReloadListener(tableView))
    }
}
