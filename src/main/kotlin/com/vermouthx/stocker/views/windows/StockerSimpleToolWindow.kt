package com.vermouthx.stocker.views.windows

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.vermouthx.stocker.actions.StockerRefreshAction
import com.vermouthx.stocker.actions.StockerStockManageAction
import com.vermouthx.stocker.actions.StockerStockSearchAction
import com.vermouthx.stocker.actions.StockerStopAction
import com.vermouthx.stocker.views.StockerTableView

class StockerSimpleToolWindow : SimpleToolWindowPanel(true) {
    var tableView: StockerTableView = StockerTableView()

    init {
        val actionManager = ActionManager.getInstance()
        val actionGroup = DefaultActionGroup(
            listOf(StockerRefreshAction::class.qualifiedName?.let { actionManager.getAction(it) },
                StockerStopAction::class.qualifiedName?.let { actionManager.getAction(it) },
                StockerStockManageAction::class.qualifiedName?.let { actionManager.getAction(it) },
                StockerStockSearchAction::class.qualifiedName?.let { actionManager.getAction(it) })
        )
        val actionToolbar = actionManager.createActionToolbar(ActionPlaces.TOOLWINDOW_CONTENT, actionGroup, true)
        actionToolbar.targetComponent = tableView.component
        this.toolbar = actionToolbar.component
        setContent(tableView.component)
    }
}
