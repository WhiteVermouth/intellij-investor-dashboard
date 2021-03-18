package com.vermouthx.stocker.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.vermouthx.stocker.enums.StockerMarketType
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
        val dialog = StockerStockDeleteDialog(project)
        val quotes = StockerQuoteHttpUtil.get(StockerMarketType.AShare, setting.quoteProvider, setting.aShareList)
        dialog.setupStockSymbols(quotes)
        dialog.show()
    }
}