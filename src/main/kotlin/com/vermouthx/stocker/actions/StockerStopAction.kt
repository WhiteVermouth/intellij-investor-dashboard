package com.vermouthx.stocker.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.vermouthx.stocker.StockerApp
import com.vermouthx.stocker.StockerBundle

class StockerStopAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val project = e.project
        val presentation = e.presentation
        presentation.text = StockerBundle.message("action.stop.refresh")
        presentation.description = StockerBundle.message("action.stop.refresh.description")
        presentation.isEnabled = project != null && !StockerApp.instance.isShutdown()
    }

    override fun actionPerformed(e: AnActionEvent) {
        StockerApp.instance.shutdown()
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
