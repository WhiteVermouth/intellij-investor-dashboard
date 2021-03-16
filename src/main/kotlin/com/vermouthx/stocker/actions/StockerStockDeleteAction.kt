package com.vermouthx.stocker.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.listeners.StockerQuoteDeleteNotifier
import com.vermouthx.stocker.settings.StockerSetting
import com.vermouthx.stocker.utils.StockerQuoteHttpUtil
import com.vermouthx.stocker.views.StockerStockDeleteDialog

class StockerStockDeleteAction : AnAction() {
    override fun update(e: AnActionEvent) {
        val project = e.project
        val presentation = e.presentation
        if (project == null) {
            presentation.isEnabled = false
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val setting = StockerSetting.instance
        val messageBus = ApplicationManager.getApplication().messageBus
        val dialog = StockerStockDeleteDialog(project)
        val quotes = StockerQuoteHttpUtil.get(StockerMarketType.AShare, setting.quoteProvider, setting.aShareList)
        dialog.setupStockSymbols(quotes)
        if (dialog.showAndGet()) {
            val deletedSymbols = dialog.deleteSymbols()
            val publisher = when (dialog.currentMarketSelection) {
                StockerMarketType.AShare, null -> messageBus.syncPublisher(StockerQuoteDeleteNotifier.STOCK_CN_QUOTE_DELETE_TOPIC)
                StockerMarketType.HKStocks -> messageBus.syncPublisher(StockerQuoteDeleteNotifier.STOCK_HK_QUOTE_DELETE_TOPIC)
                StockerMarketType.USStocks -> messageBus.syncPublisher(StockerQuoteDeleteNotifier.STOCK_US_QUOTE_DELETE_TOPIC)
            }
            val publisherToAll = messageBus.syncPublisher(StockerQuoteDeleteNotifier.STOCK_ALL_QUOTE_DELETE_TOPIC)
            deletedSymbols.forEach {
                publisherToAll.after(it.toUpperCase())
                publisher.after(it.toUpperCase())
            }
        }
    }
}