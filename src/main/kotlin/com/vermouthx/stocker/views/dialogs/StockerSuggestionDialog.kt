package com.vermouthx.stocker.views.dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.JBColor
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
import com.vermouthx.stocker.StockerAppManager
import com.vermouthx.stocker.entities.StockerSuggestion
import com.vermouthx.stocker.enums.StockerStockOperation
import com.vermouthx.stocker.settings.StockerSetting
import com.vermouthx.stocker.utils.StockerActionUtil
import com.vermouthx.stocker.utils.StockerSuggestHttpUtil
import java.util.concurrent.Executors
import javax.swing.Action
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JSeparator
import javax.swing.event.DocumentEvent

class StockerSuggestionDialog(val project: Project?) : DialogWrapper(project) {

    private val service = Executors.newFixedThreadPool(1)
    private val setting = StockerSetting.instance

    private lateinit var suggestions: List<StockerSuggestion>

    init {
        title = "Search Stocks"
        init()
    }

    override fun createCenterPanel(): DialogPanel {
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

        suggestions = StockerSuggestHttpUtil.suggest("02400", setting.quoteProvider)
        refreshScrollPane(scrollPane)

        return panel {
            row {
                searchTextField(CCFlags.growX)
            }
            row {
                scrollPane(scrollPane)
            }
        }.withPreferredWidth(400).withPreferredHeight(500)
    }

    override fun createActions(): Array<Action> {
        return emptyArray()
    }

    private fun refreshScrollPane(scrollPane: JBScrollPane) {
        scrollPane.setViewportView(
            panel {
                suggestions.forEach { suggestion ->
                    val actionButton = JButton()
                    val separator = JSeparator().also {
                        it.foreground = JBColor.border()
                        it.background = JBColor.background()
                    }
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
                                        myApplication.schedule()
                                        return@addActionListener
                                    }
                                }
                                myApplication.schedule()
                            }
                        }
                        right {
                            actionButton()
                        }
                    }
                    row {
                        separator(CCFlags.growX)
                    }
                }
            }.withBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16))
        )
    }

}
