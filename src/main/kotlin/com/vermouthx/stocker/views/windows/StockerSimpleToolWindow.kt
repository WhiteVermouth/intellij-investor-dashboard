package com.vermouthx.stocker.views.windows

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.vermouthx.stocker.actions.StockerRefreshAction
import com.vermouthx.stocker.actions.StockerSettingAction
import com.vermouthx.stocker.actions.StockerStockManageAction
import com.vermouthx.stocker.actions.StockerStockSearchAction
import com.vermouthx.stocker.views.StockerTableView

class StockerSimpleToolWindow : SimpleToolWindowPanel(true) {
    var tableView: StockerTableView = StockerTableView()

    init {
        val actionManager = ActionManager.getInstance()
        val leftActions = listOfNotNull(
            StockerStockSearchAction::class.qualifiedName?.let { actionManager.getAction(it) },
            StockerRefreshAction::class.qualifiedName?.let { actionManager.getAction(it) },
            StockerStockManageAction::class.qualifiedName?.let { actionManager.getAction(it) }
        )
        val actionGroup = DefaultActionGroup(leftActions)
        val actionToolbar = actionManager.createActionToolbar(ActionPlaces.TOOLWINDOW_CONTENT, actionGroup, true)
        actionToolbar.targetComponent = tableView.component
        
        val rightActionGroup = DefaultActionGroup().apply {
            StockerSettingAction::class.qualifiedName?.let { actionManager.getAction(it) }?.let { add(it) }
        }
        val rightActionToolbar = actionManager.createActionToolbar(ActionPlaces.TOOLWINDOW_CONTENT, rightActionGroup, true)
        rightActionToolbar.targetComponent = tableView.component
        
        val toolbarPanel = com.intellij.ui.components.panels.HorizontalLayout(0).let { layout ->
            javax.swing.JPanel(java.awt.BorderLayout()).apply {
                add(actionToolbar.component, java.awt.BorderLayout.WEST)
                add(rightActionToolbar.component, java.awt.BorderLayout.EAST)
            }
        }
        
        this.toolbar = toolbarPanel
        setContent(tableView.component)
    }
}
