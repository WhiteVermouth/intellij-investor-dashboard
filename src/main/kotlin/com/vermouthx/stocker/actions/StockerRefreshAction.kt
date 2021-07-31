package com.vermouthx.stocker.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.vermouthx.stocker.StockerAppManager

class StockerRefreshAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val project = e.project
        val presentation = e.presentation
        if (project == null) {
            presentation.isEnabled = false
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        StockerAppManager.myApplicationMap[e.project]?.shutdown()
        StockerAppManager.myApplicationMap[e.project]?.schedule()
    }
}