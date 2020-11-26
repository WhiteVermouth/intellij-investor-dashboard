package com.vermouthx.stocker.views

import com.intellij.openapi.ui.SimpleToolWindowPanel

class StockerSimpleToolWindow(var tableView: StockerTableView) : SimpleToolWindowPanel(false) {
    init {
        setContent(tableView.content)
    }
}