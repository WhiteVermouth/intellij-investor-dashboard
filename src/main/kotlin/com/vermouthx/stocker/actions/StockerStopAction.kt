package com.vermouthx.stocker.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.vermouthx.stocker.StockerAppManager
import com.vermouthx.stocker.StockerBundle

class StockerStopAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val project = e.project
        val presentation = e.presentation
        presentation.text = StockerBundle.message("action.stop.refresh")
        presentation.description = StockerBundle.message("action.stop.refresh.description")
        presentation.isEnabled = project != null && StockerAppManager.myApplication(project)?.isShutdown() == false
    }

    override fun actionPerformed(e: AnActionEvent) {
        StockerAppManager.myApplication(e.project)?.shutdown()
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
