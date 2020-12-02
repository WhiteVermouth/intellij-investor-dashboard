package com.vermouthx.stocker.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.IconLoader
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.listeners.StockerQuoteDeleteNotifier
import com.vermouthx.stocker.settings.StockerSetting
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
        if (dialog.showAndGet()) {
            val market = dialog.market
            val codes = dialog.input
            codes.split(",").forEach {
                when (market) {
                    StockerMarketType.AShare -> {
                        if (setting.aShareList.contains(it.toUpperCase())) {
                            setting.removeCode(market, it.toUpperCase())
                            val publisher =
                                messageBus.syncPublisher(StockerQuoteDeleteNotifier.STOCK_CN_QUOTE_DELETE_TOPIC)
                            publisher.after(it.toUpperCase())
                        } else {
                            Messages.showMessageDialog(
                                project,
                                "You entered an non-existent stock code: ${it}.",
                                "Invalid Stock Code",
                                IconLoader.getIcon("/icons/logo.svg", javaClass)
                            )
                        }
                    }
                    StockerMarketType.HKStocks -> {
                        if (setting.hkStocksList.contains(it.toUpperCase())) {
                            setting.removeCode(market, it.toUpperCase())
                            val publisher =
                                messageBus.syncPublisher(StockerQuoteDeleteNotifier.STOCK_HK_QUOTE_DELETE_TOPIC)
                            publisher.after(it.toUpperCase())
                        } else {
                            Messages.showMessageDialog(
                                project,
                                "You entered an non-existent stock code: ${it}.",
                                "Invalid Stock Code",
                                IconLoader.getIcon("/icons/logo.svg", javaClass)
                            )
                        }
                    }
                    StockerMarketType.USStocks -> {
                        if (setting.usStocksList.contains(it.toUpperCase())) {
                            setting.removeCode(market, it.toUpperCase())
                            val publisher =
                                messageBus.syncPublisher(StockerQuoteDeleteNotifier.STOCK_US_QUOTE_DELETE_TOPIC)
                            publisher.after(it.toUpperCase())
                        } else {
                            Messages.showMessageDialog(
                                project,
                                "You entered an non-existent stock code: ${it}.",
                                "Invalid Stock Code",
                                IconLoader.getIcon("/icons/logo.svg", javaClass)
                            )
                        }
                    }
                }
            }
        }
    }
}