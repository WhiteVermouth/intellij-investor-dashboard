package com.vermouthx.stocker.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.IconLoader
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.settings.StockerSetting
import com.vermouthx.stocker.utils.StockerQuoteHttpUtil
import com.vermouthx.stocker.views.StockerStockAddDialog

class StockerStockAddAction : AnAction() {
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
        val dialog = StockerStockAddDialog(project)
        if (dialog.showAndGet()) {
            val market = dialog.market
            val codes = dialog.input
            codes.split(",").forEach {
                if (StockerQuoteHttpUtil.validateCode(market, StockerSetting.instance.quoteProvider, it)) {
                    when (market) {
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
                    Messages.showMessageDialog(
                        project,
                        "You entered an invalid stock code: ${it}.",
                        "Invalid Stock Code",
                        IconLoader.getIcon("/icons/logo.svg")
                    )
                }
            }
        }
    }
}