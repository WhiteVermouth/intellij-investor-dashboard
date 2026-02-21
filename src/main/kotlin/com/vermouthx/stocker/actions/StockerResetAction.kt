package com.vermouthx.stocker.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.vermouthx.stocker.StockerAppManager
import com.vermouthx.stocker.StockerBundle
import com.vermouthx.stocker.settings.StockerSetting

class StockerResetAction : AnAction() {
    override fun update(e: AnActionEvent) {
        val project = e.project
        val presentation = e.presentation
        presentation.text = StockerBundle.message("action.remove.all.favorite.stocks")
        presentation.description = StockerBundle.message("action.remove.all.favorite.stocks.description")
        presentation.isEnabled = project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val myApplication = StockerAppManager.myApplication(e.project)
        myApplication?.shutdownThenClear()

        val setting = StockerSetting.instance
        setting.aShareList.clear()
        setting.hkStocksList.clear()
        setting.usStocksList.clear()
        setting.cryptoList.clear()

        myApplication?.schedule()
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
