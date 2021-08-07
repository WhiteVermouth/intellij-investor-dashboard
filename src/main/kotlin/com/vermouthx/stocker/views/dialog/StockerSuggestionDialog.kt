package com.vermouthx.stocker.views.dialog

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
import com.vermouthx.stocker.StockerAppManager
import com.vermouthx.stocker.entities.StockerSuggest
import com.vermouthx.stocker.enums.StockerQuoteProvider
import com.vermouthx.stocker.enums.StockerStockOperation
import com.vermouthx.stocker.settings.StockerSetting
import com.vermouthx.stocker.utils.StockerActionUtil
import com.vermouthx.stocker.utils.StockerSuggestHttpUtil
import java.util.concurrent.Executors
import javax.swing.Action
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.event.DocumentEvent

class StockerSuggestionDialog(val project: Project?) : DialogWrapper(project) {

    init {
        title = "Search Stocks"
        init()
    }

    private val service = Executors.newFixedThreadPool(1)

    private lateinit var suggestions: List<StockerSuggest>

    override fun createCenterPanel(): DialogPanel {
        val searchTextField = SearchTextField(true)
        val scrollPane = JBScrollPane()

        searchTextField.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                service.submit {
                    val text = searchTextField.text
                    if (!text.isNullOrEmpty()) {
                        suggestions = StockerSuggestHttpUtil.suggest(text, StockerQuoteProvider.SINA)
                        refreshScrollPane(scrollPane)
                    }
                }
            }
        })

        suggestions = StockerSuggestHttpUtil.suggest("SH600", StockerQuoteProvider.SINA)
        refreshScrollPane(scrollPane)

        return panel {
            row {
                searchTextField(CCFlags.growX)
            }
            row {
                scrollPane(scrollPane)
            }
        }.withPreferredHeight(500)
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
                        ).constraints(CCFlags.pushX)
                        if (StockerSetting.instance.containsCode(suggestion.code)) {
                            actionButton.text = StockerStockOperation.STOCK_DELETE.operation
                        } else {
                            actionButton.text = StockerStockOperation.STOCK_ADD.operation
                        }
                        actionButton.addActionListener {
                            val myApplication = StockerAppManager.myApplication(project)
                            if (myApplication != null) {
                                myApplication.shutdown()
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
                                    }
                                }
                                myApplication.schedule()
                            }
                        }
                        actionButton()
                    }
                }
            }.withBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8))
        )
    }

}