package com.vermouthx.stocker.views.dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.vermouthx.stocker.StockerAppManager
import com.vermouthx.stocker.entities.StockerSuggestion
import com.vermouthx.stocker.enums.StockerStockOperation
import com.vermouthx.stocker.settings.StockerSetting
import com.vermouthx.stocker.utils.StockerActionUtil
import com.vermouthx.stocker.utils.StockerSuggestHttpUtil
import java.awt.BorderLayout
import java.awt.Dimension
import java.util.concurrent.Executors
import javax.swing.Action
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.event.DocumentEvent

class StockerSuggestionDialog(val project: Project?) : DialogWrapper(project) {

    private val service = Executors.newFixedThreadPool(1)
    private val setting = StockerSetting.instance

    private var suggestions: List<StockerSuggestion> = emptyList()

    init {
        title = "Search Stocks"
        init()
    }

    override fun createCenterPanel(): DialogPanel {
        val dialogPanel = DialogPanel(BorderLayout())
        val searchTextField = SearchTextField(true)
        val scrollPane = JBScrollPane()

        searchTextField.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                service.submit {
                    val text = searchTextField.text.trim()
                    if (text.isNotEmpty()) {
                        suggestions = StockerSuggestHttpUtil.suggest(text, setting.quoteProvider)
                        refreshScrollPane(scrollPane)
                    }
                }
            }
        })

        suggestions = StockerSuggestHttpUtil.suggest("600", setting.quoteProvider)
        refreshScrollPane(scrollPane)

        searchTextField.border = BorderFactory.createEmptyBorder(0, 0, 8, 0)
        dialogPanel.add(searchTextField, BorderLayout.NORTH)
        dialogPanel.add(scrollPane, BorderLayout.CENTER)
        dialogPanel.preferredSize = Dimension(300, 500)
        return dialogPanel
    }

    override fun createActions(): Array<Action> {
        return emptyArray()
    }

    private fun refreshScrollPane(scrollPane: JBScrollPane) {
        scrollPane.setViewportView(
            panel {
                suggestions.forEach { suggestion ->
                    val actionButton = JButton()
                    row {
                        label(suggestion.code)
                        label(
                            if (suggestion.name.length <= 20) {
                                suggestion.name
                            } else {
                                "${suggestion.name.substring(0, 20)}..."
                            }
                        )
                        if (StockerSetting.instance.containsCode(suggestion.code)) {
                            actionButton.text = StockerStockOperation.STOCK_DELETE.operation
                        } else {
                            actionButton.text = StockerStockOperation.STOCK_ADD.operation
                        }
                        actionButton.addActionListener {
                            val myApplication = StockerAppManager.myApplication(project)
                            if (myApplication != null) {
                                myApplication.shutdownThenClear()
                                when (StockerStockOperation.mapOf(actionButton.text)) {
                                    StockerStockOperation.STOCK_ADD -> {
                                        StockerActionUtil.addStock(suggestion.market, suggestion, project)
                                        actionButton.text = StockerStockOperation.STOCK_DELETE.operation
                                    }

                                    StockerStockOperation.STOCK_DELETE -> {
                                        StockerActionUtil.removeStock(suggestion.market, suggestion)
                                        actionButton.text = StockerStockOperation.STOCK_ADD.operation
                                    }

                                    else -> {
                                        myApplication.schedule()
                                        return@addActionListener
                                    }
                                }
                                myApplication.schedule()
                            }
                        }
                        cell(actionButton).align(AlignX.RIGHT)
                    }
                    separator()
                }
            }.withBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8))
        )
    }

}
