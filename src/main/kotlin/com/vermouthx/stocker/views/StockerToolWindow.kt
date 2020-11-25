package com.vermouthx.stocker.views

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.listeners.StockerQuoteListener
import com.vermouthx.stocker.listeners.StockerQuoteUpdateNotifier
import com.vermouthx.stocker.notifications.StockerNotification
import com.vermouthx.stocker.settings.StockerSetting
import com.vermouthx.stocker.utils.StockerQuoteHttpUtil

class StockerToolWindow : ToolWindowFactory {

    companion object {
        val setting = StockerSetting.instance
        val messageBus = ApplicationManager.getApplication().messageBus
    }

    private lateinit var tabViewMap: Map<StockerMarketType, StockerUIView>

    override fun init(toolWindow: ToolWindow) {
        super.init(toolWindow)
        tabViewMap = mapOf(
                StockerMarketType.AShare to StockerUIView(),
                StockerMarketType.HKStocks to StockerUIView(),
                StockerMarketType.USStocks to StockerUIView()
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
                    if (StockerQuoteHttpUtil.validateCode(k, setting.quoteProvider, input)) {
                        val setting = StockerSetting.instance
                        when (k) {
                            StockerMarketType.AShare -> setting.aShareList.add(input.toUpperCase())
                            StockerMarketType.HKStocks -> setting.hkStocksList.add(input.toUpperCase())
                            StockerMarketType.USStocks -> setting.usStocksList.add(input.toUpperCase())
                        }
                    } else {
                        StockerNotification.notifyInvalidCode(project, input)
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
}