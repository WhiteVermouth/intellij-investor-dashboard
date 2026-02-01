package com.vermouthx.stocker.views.windows

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.util.messages.MessageBusConnection
import com.vermouthx.stocker.StockerApp
import com.vermouthx.stocker.StockerAppManager
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.listeners.StockerQuoteDeleteListener
import com.vermouthx.stocker.listeners.StockerQuoteDeleteNotifier.*
import com.vermouthx.stocker.listeners.StockerQuoteReloadListener
import com.vermouthx.stocker.listeners.StockerQuoteReloadNotifier.*
import com.vermouthx.stocker.listeners.StockerQuoteUpdateListener
import com.vermouthx.stocker.listeners.StockerQuoteUpdateNotifier.*

class StockerToolWindow : ToolWindowFactory {

    private val messageBus = ApplicationManager.getApplication().messageBus

    private lateinit var allView: StockerSimpleToolWindow
    private lateinit var tabViewMap: Map<StockerMarketType, StockerSimpleToolWindow>
    private lateinit var myApplication: StockerApp
    private val messageBusConnections = mutableListOf<MessageBusConnection>()

    override fun init(toolWindow: ToolWindow) {
        super.init(toolWindow)
        allView = StockerSimpleToolWindow()
        tabViewMap = mapOf(
            StockerMarketType.AShare to StockerSimpleToolWindow(),
            StockerMarketType.HKStocks to StockerSimpleToolWindow(),
            StockerMarketType.USStocks to StockerSimpleToolWindow(),
            StockerMarketType.Crypto to StockerSimpleToolWindow()
        )
        myApplication = StockerApp()
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentManager = toolWindow.contentManager
        val contentFactory = ContentFactory.getInstance()
        
        // Create a disposable for cleanup when tool window is closed
        val disposable = Disposer.newDisposable("StockerToolWindow")
        toolWindow.disposable.let { Disposer.register(it, disposable) }
        
        val allContent = contentFactory.createContent(allView.component, "ALL", false)
        contentManager.addContent(allContent)
        val aShareContent = contentFactory.createContent(
            tabViewMap[StockerMarketType.AShare]?.component, StockerMarketType.AShare.title, false
        )
        contentManager.addContent(aShareContent)
        val hkStocksContent = contentFactory.createContent(
            tabViewMap[StockerMarketType.HKStocks]?.component, StockerMarketType.HKStocks.title, false
        )
        contentManager.addContent(hkStocksContent)
        val usStocksContent = contentFactory.createContent(
            tabViewMap[StockerMarketType.USStocks]?.component, StockerMarketType.USStocks.title, false
        )
        contentManager.addContent(usStocksContent)
        val cryptoContent = contentFactory.createContent(
            tabViewMap[StockerMarketType.Crypto]?.component,
            StockerMarketType.Crypto.title,
            false
        )
        contentManager.addContent(cryptoContent)
        this.subscribeMessage()
        
        // Register cleanup when disposable is disposed
        Disposer.register(disposable) {
            cleanup()
        }
        
        StockerAppManager.register(project, myApplication)
        myApplication.schedule()
    }
    
    private fun cleanup() {
        // Dispose all table views
        allView.tableView.dispose()
        tabViewMap.values.forEach { it.tableView.dispose() }
        
        // Disconnect all message bus connections
        messageBusConnections.forEach { it.disconnect() }
        messageBusConnections.clear()
    }

    private fun subscribeMessage() {
        // Create and store connections for proper disposal
        messageBusConnections.add(messageBus.connect().apply {
            subscribe(STOCK_ALL_QUOTE_UPDATE_TOPIC, StockerQuoteUpdateListener(allView.tableView))
        })
        messageBusConnections.add(messageBus.connect().apply {
            subscribe(STOCK_ALL_QUOTE_DELETE_TOPIC, StockerQuoteDeleteListener(allView.tableView))
        })
        messageBusConnections.add(messageBus.connect().apply {
            subscribe(STOCK_ALL_QUOTE_RELOAD_TOPIC, StockerQuoteReloadListener(allView.tableView))
        })
        
        tabViewMap.forEach { (market, myTableView) ->
            when (market) {
                StockerMarketType.AShare -> {
                    messageBusConnections.add(messageBus.connect().apply {
                        subscribe(STOCK_CN_QUOTE_UPDATE_TOPIC, StockerQuoteUpdateListener(myTableView.tableView))
                    })
                    messageBusConnections.add(messageBus.connect().apply {
                        subscribe(STOCK_CN_QUOTE_DELETE_TOPIC, StockerQuoteDeleteListener(myTableView.tableView))
                    })
                    messageBusConnections.add(messageBus.connect().apply {
                        subscribe(STOCK_CN_QUOTE_RELOAD_TOPIC, StockerQuoteReloadListener(myTableView.tableView))
                    })
                }

                StockerMarketType.HKStocks -> {
                    messageBusConnections.add(messageBus.connect().apply {
                        subscribe(STOCK_HK_QUOTE_UPDATE_TOPIC, StockerQuoteUpdateListener(myTableView.tableView))
                    })
                    messageBusConnections.add(messageBus.connect().apply {
                        subscribe(STOCK_HK_QUOTE_DELETE_TOPIC, StockerQuoteDeleteListener(myTableView.tableView))
                    })
                    messageBusConnections.add(messageBus.connect().apply {
                        subscribe(STOCK_HK_QUOTE_RELOAD_TOPIC, StockerQuoteReloadListener(myTableView.tableView))
                    })
                }

                StockerMarketType.USStocks -> {
                    messageBusConnections.add(messageBus.connect().apply {
                        subscribe(STOCK_US_QUOTE_UPDATE_TOPIC, StockerQuoteUpdateListener(myTableView.tableView))
                    })
                    messageBusConnections.add(messageBus.connect().apply {
                        subscribe(STOCK_US_QUOTE_DELETE_TOPIC, StockerQuoteDeleteListener(myTableView.tableView))
                    })
                    messageBusConnections.add(messageBus.connect().apply {
                        subscribe(STOCK_US_QUOTE_RELOAD_TOPIC, StockerQuoteReloadListener(myTableView.tableView))
                    })
                }

                StockerMarketType.Crypto -> {
                    messageBusConnections.add(messageBus.connect().apply {
                        subscribe(CRYPTO_QUOTE_UPDATE_TOPIC, StockerQuoteUpdateListener(myTableView.tableView))
                    })
                    messageBusConnections.add(messageBus.connect().apply {
                        subscribe(CRYPTO_QUOTE_DELETE_TOPIC, StockerQuoteDeleteListener(myTableView.tableView))
                    })
                    messageBusConnections.add(messageBus.connect().apply {
                        subscribe(STOCK_CRYPTO_QUOTE_RELOAD_TOPIC, StockerQuoteReloadListener(myTableView.tableView))
                    })
                }
            }
        }
    }
}
