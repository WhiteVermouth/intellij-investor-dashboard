package com.vermouthx.stocker.views

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class StockerToolWindow : ToolWindowFactory {

    enum class StockerStockType(val title: String) {
        AShare("A Share"),
        HKStocks("H.K Stocks"),
        USStocks("U.S. Stocks")
    }

    companion object {
        val tabViewMap: Map<StockerStockType, StockerUIView> = mapOf(
                StockerStockType.AShare to StockerUIView(),
                StockerStockType.HKStocks to StockerUIView(),
                StockerStockType.USStocks to StockerUIView()
        )
    }

    override fun init(toolWindow: ToolWindow) {
        super.init(toolWindow)
        tabViewMap.forEach { (k, v) ->
            v.btnAdd.addActionListener {
                val dialog = StockerStockAddDialog()
                if (dialog.showAndGet()) {

                }
            }
            v.btnRefresh.addActionListener {

            }
        }
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentManager = toolWindow.contentManager
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val aShareContent = contentFactory.createContent(tabViewMap[StockerStockType.AShare]?.content, StockerStockType.AShare.title, false)
        contentManager.addContent(aShareContent)
        val hkStocksContent = contentFactory.createContent(tabViewMap[StockerStockType.HKStocks]?.content, StockerStockType.HKStocks.title, false)
        contentManager.addContent(hkStocksContent)
        val usStocksContent = contentFactory.createContent(tabViewMap[StockerStockType.USStocks]?.content, StockerStockType.USStocks.title, false)
        contentManager.addContent(usStocksContent)
    }
}