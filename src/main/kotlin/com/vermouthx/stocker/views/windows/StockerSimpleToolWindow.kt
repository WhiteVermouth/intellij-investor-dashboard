package com.vermouthx.stocker.views.windows

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.vermouthx.stocker.actions.StockerRefreshAction
import com.vermouthx.stocker.actions.StockerSettingAction
import com.vermouthx.stocker.actions.StockerStockManageAction
import com.vermouthx.stocker.actions.StockerStockSearchAction
import com.vermouthx.stocker.actions.StockerStopAction
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.views.StockerTableView
import java.awt.BorderLayout
import javax.swing.JPanel

class StockerSimpleToolWindow(market: StockerMarketType? = null) : SimpleToolWindowPanel(true) {
    var tableView: StockerTableView = StockerTableView(market)

    init {
        val actionManager = ActionManager.getInstance()
        val leftActions = listOfNotNull(
            StockerStockSearchAction::class.qualifiedName?.let { actionManager.getAction(it) },
            StockerRefreshAction::class.qualifiedName?.let { actionManager.getAction(it) },
            StockerStopAction::class.qualifiedName?.let { actionManager.getAction(it) },
            StockerStockManageAction::class.qualifiedName?.let { actionManager.getAction(it) }
        )
        val actionGroup = DefaultActionGroup(leftActions)
        val actionToolbar = actionManager.createActionToolbar(ActionPlaces.TOOLWINDOW_CONTENT, actionGroup, true)
        actionToolbar.targetComponent = tableView.component

        val rightActionGroup = DefaultActionGroup().apply {
            StockerSettingAction::class.qualifiedName?.let { actionManager.getAction(it) }?.let { add(it) }
        }
        val rightActionToolbar =
            actionManager.createActionToolbar(ActionPlaces.TOOLWINDOW_CONTENT, rightActionGroup, true)
        rightActionToolbar.targetComponent = tableView.component

        this.toolbar = JPanel(BorderLayout()).apply {
            add(actionToolbar.component, BorderLayout.WEST)
            add(rightActionToolbar.component, BorderLayout.EAST)
        }
        setContent(tableView.component)
    }
}
