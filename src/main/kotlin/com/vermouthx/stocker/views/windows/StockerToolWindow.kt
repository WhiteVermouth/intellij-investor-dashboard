package com.vermouthx.stocker.views.windows

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.vermouthx.stocker.StockerApp
import com.vermouthx.stocker.StockerAppManager
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.listeners.StockerQuoteDeleteListener
import com.vermouthx.stocker.listeners.StockerQuoteDeleteNotifier.*
import com.vermouthx.stocker.listeners.StockerQuoteReloadListener
import com.vermouthx.stocker.listeners.StockerQuoteReloadNotifier.*
import com.vermouthx.stocker.listeners.StockerQuoteUpdateListener
import com.vermouthx.stocker.listeners.StockerQuoteUpdateNotifier.*
import com.vermouthx.stocker.settings.StockerSetting

class StockerToolWindow : ToolWindowFactory {

    private val messageBus = ApplicationManager.getApplication().messageBus

    private lateinit var allView: StockerSimpleToolWindow
    private lateinit var tabViewMap: Map<StockerMarketType, StockerSimpleToolWindow>
    private lateinit var myApplication: StockerApp

    private fun injectPopupMenu(project: Project?, window: StockerSimpleToolWindow?) {
        if (window != null) {
            val tbBody = window.tableView.tableBody
            val tbModel = window.tableView.tableModel
            val tbPopupMenu = JBPopupMenu()
            val tbPopupDeleteMenuItem = JBMenuItem("Delete", AllIcons.General.Remove)
            tbPopupDeleteMenuItem.addActionListener {
                if (tbBody.selectedRowCount == 0) {
                    Messages.showErrorDialog(
                        project, "You have not selected any stock symbol.", "Require Symbol Selection"
                    )
                    return@addActionListener
                }
                val setting = StockerSetting.instance
                for (selectedRow in tbBody.selectedRows) {
                    val code = tbModel.getValueAt(selectedRow, 0).toString()
                    val market = setting.marketOf(code)
                    if (market != null) {
                        myApplication.shutdown()
                        setting.removeCode(market, code)
                        when (market) {
                            StockerMarketType.AShare -> {
                                val publisher = messageBus.syncPublisher(STOCK_CN_QUOTE_DELETE_TOPIC)
                                publisher.after(code)
                            }

                            StockerMarketType.HKStocks -> {
                                val publisher = messageBus.syncPublisher(STOCK_HK_QUOTE_DELETE_TOPIC)
                                publisher.after(code)
                            }

                            StockerMarketType.USStocks -> {
                                val publisher = messageBus.syncPublisher(STOCK_US_QUOTE_DELETE_TOPIC)
                                publisher.after(code)
                            }

                            StockerMarketType.Crypto -> {
                                val publisher = messageBus.syncPublisher(CRYPTO_QUOTE_DELETE_TOPIC)
                                publisher.after(code)
                            }
                        }
                        val publisher = messageBus.syncPublisher(STOCK_ALL_QUOTE_DELETE_TOPIC)
                        publisher.after(code)
                        myApplication.schedule()
                    }
                }
            }
            tbPopupMenu.add(tbPopupDeleteMenuItem)
            tbBody.componentPopupMenu = tbPopupMenu
        }
    }

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
        val allContent =
            contentFactory.createContent(allView.component, "ALL", false).also { injectPopupMenu(project, allView) }
        contentManager.addContent(allContent)
        val aShareContent = contentFactory.createContent(
            tabViewMap[StockerMarketType.AShare]?.component, StockerMarketType.AShare.title, false
        ).also {
            injectPopupMenu(project, tabViewMap[StockerMarketType.AShare])
        }
        contentManager.addContent(aShareContent)
        val hkStocksContent = contentFactory.createContent(
            tabViewMap[StockerMarketType.HKStocks]?.component, StockerMarketType.HKStocks.title, false
        ).also {
            injectPopupMenu(project, tabViewMap[StockerMarketType.HKStocks])
        }
        contentManager.addContent(hkStocksContent)
        val usStocksContent = contentFactory.createContent(
            tabViewMap[StockerMarketType.USStocks]?.component, StockerMarketType.USStocks.title, false
        ).also {
            injectPopupMenu(project, tabViewMap[StockerMarketType.USStocks])
        }
        contentManager.addContent(usStocksContent)
//        val cryptoContent = contentFactory.createContent(
//            tabViewMap[StockerMarketType.Crypto]?.component,
//            StockerMarketType.Crypto.title,
//            false
//        ).also {
//            injectPopupMenu(project, tabViewMap[StockerMarketType.Crypto])
//        }
//        contentManager.addContent(cryptoContent)
        this.subscribeMessage()
        StockerAppManager.myApplicationMap[project] = myApplication
        myApplication.schedule()
    }

    private fun subscribeMessage() {
        messageBus.connect().subscribe(
            STOCK_ALL_QUOTE_UPDATE_TOPIC, StockerQuoteUpdateListener(allView.tableView)
        )
        messageBus.connect().subscribe(
            STOCK_ALL_QUOTE_DELETE_TOPIC, StockerQuoteDeleteListener(allView.tableView)
        )
        messageBus.connect().subscribe(
            STOCK_ALL_QUOTE_RELOAD_TOPIC, StockerQuoteReloadListener(allView.tableView)
        )
        tabViewMap.forEach { (market, myTableView) ->
            when (market) {
                StockerMarketType.AShare -> {
                    messageBus.connect().subscribe(
                        STOCK_CN_QUOTE_UPDATE_TOPIC, StockerQuoteUpdateListener(
                            myTableView.tableView
                        )
                    )
                    messageBus.connect().subscribe(
                        STOCK_CN_QUOTE_DELETE_TOPIC, StockerQuoteDeleteListener(
                            myTableView.tableView
                        )
                    )
                    messageBus.connect().subscribe(
                        STOCK_CN_QUOTE_RELOAD_TOPIC, StockerQuoteReloadListener(myTableView.tableView)
                    )
                }

                StockerMarketType.HKStocks -> {
                    messageBus.connect().subscribe(
                        STOCK_HK_QUOTE_UPDATE_TOPIC, StockerQuoteUpdateListener(
                            myTableView.tableView
                        )
                    )
                    messageBus.connect().subscribe(
                        STOCK_HK_QUOTE_DELETE_TOPIC, StockerQuoteDeleteListener(
                            myTableView.tableView
                        )
                    )
                    messageBus.connect().subscribe(
                        STOCK_HK_QUOTE_RELOAD_TOPIC, StockerQuoteReloadListener(myTableView.tableView)
                    )
                }

                StockerMarketType.USStocks -> {
                    messageBus.connect().subscribe(
                        STOCK_US_QUOTE_UPDATE_TOPIC, StockerQuoteUpdateListener(
                            myTableView.tableView
                        )
                    )
                    messageBus.connect().subscribe(
                        STOCK_US_QUOTE_DELETE_TOPIC, StockerQuoteDeleteListener(
                            myTableView.tableView
                        )
                    )
                    messageBus.connect().subscribe(
                        STOCK_US_QUOTE_RELOAD_TOPIC, StockerQuoteReloadListener(
                            myTableView.tableView
                        )
                    )
                }

                StockerMarketType.Crypto -> {
                    messageBus.connect().subscribe(
                        CRYPTO_QUOTE_UPDATE_TOPIC, StockerQuoteUpdateListener(
                            myTableView.tableView
                        )
                    )
                    messageBus.connect().subscribe(
                        CRYPTO_QUOTE_DELETE_TOPIC, StockerQuoteDeleteListener(
                            myTableView.tableView
                        )
                    )
                    messageBus.connect().subscribe(
                        STOCK_CRYPTO_QUOTE_RELOAD_TOPIC, StockerQuoteReloadListener(
                            myTableView.tableView
                        )
                    )
                }
            }
        }
    }
}
