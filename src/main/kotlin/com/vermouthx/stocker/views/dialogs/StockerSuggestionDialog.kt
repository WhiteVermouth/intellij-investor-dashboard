package com.vermouthx.stocker.views.dialogs

import com.intellij.openapi.diagnostic.Logger
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
import com.vermouthx.stocker.utils.StockerPinyinUtil
import com.vermouthx.stocker.utils.StockerSuggestHttpUtil
import java.awt.BorderLayout
import java.awt.Dimension
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.swing.Action
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent

class StockerSuggestionDialog(val project: Project?) : DialogWrapper(project) {

    private val log = Logger.getInstance(StockerSuggestionDialog::class.java)
    private val service: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private val setting = StockerSetting.instance

    private var suggestions: List<StockerSuggestion> = emptyList()
    private var searchTask: ScheduledFuture<*>? = null
    private var isLoading: Boolean = false

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
                // Cancel any pending search task
                searchTask?.cancel(false)
                
                val text = searchTextField.text.trim()
                if (text.isEmpty()) {
                    isLoading = false
                    suggestions = emptyList()
                    SwingUtilities.invokeLater { refreshScrollPane(scrollPane) }
                    return
                }
                
                // Show loading state immediately
                isLoading = true
                SwingUtilities.invokeLater { refreshScrollPane(scrollPane) }
                
                // Debounce: schedule search after 300ms delay
                searchTask = service.schedule({
                    try {
                        val newSuggestions = StockerSuggestHttpUtil.suggest(text, setting.quoteProvider)
                        // Update UI on EDT
                        SwingUtilities.invokeLater {
                            isLoading = false
                            suggestions = newSuggestions
                            refreshScrollPane(scrollPane)
                        }
                    } catch (e: Exception) {
                        log.warn("Failed to fetch stock suggestions", e)
                        SwingUtilities.invokeLater {
                            isLoading = false
                            refreshScrollPane(scrollPane)
                        }
                    }
                }, 300, TimeUnit.MILLISECONDS)
            }
        })

        // Initialize with empty state instead of hardcoded search
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

    override fun dispose() {
        try {
            searchTask?.cancel(true)
            service.shutdown()
            if (!service.awaitTermination(1, TimeUnit.SECONDS)) {
                service.shutdownNow()
            }
        } catch (e: InterruptedException) {
            service.shutdownNow()
            Thread.currentThread().interrupt()
        }
        super.dispose()
    }

    private fun refreshScrollPane(scrollPane: JBScrollPane) {
        val usePinyin = setting.displayNameWithPinyin
        
        val contentPanel = if (isLoading) {
            panel {
                row {
                    label("Searching...").align(AlignX.CENTER)
                }
            }.withBorder(BorderFactory.createEmptyBorder(16, 8, 8, 8))
        } else if (suggestions.isEmpty()) {
            panel {
                row {
                    label("Type to search for stocks...").align(AlignX.CENTER)
                }
            }.withBorder(BorderFactory.createEmptyBorder(16, 8, 8, 8))
        } else {
            panel {
                suggestions.forEach { suggestion ->
                    val actionButton = JButton()
                    val displayName = if (usePinyin) {
                        StockerPinyinUtil.toPinyin(suggestion.name)
                    } else {
                        suggestion.name
                    }
                    
                    row {
                        label(suggestion.code)
                        label(
                            if (displayName.length <= 20) {
                                displayName
                            } else {
                                "${displayName.substring(0, 20)}..."
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
        }
        
        scrollPane.setViewportView(contentPanel)
        // Force UI refresh to prevent flickering
        scrollPane.revalidate()
        scrollPane.repaint()
    }

}
