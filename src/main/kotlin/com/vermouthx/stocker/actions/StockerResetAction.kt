package com.vermouthx.stocker.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.vermouthx.stocker.settings.StockerSetting

class StockerResetAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val setting = StockerSetting.instance
        setting.aShareList.clear()
        setting.hkStocksList.clear()
        setting.usStocksList.clear()
    }
}