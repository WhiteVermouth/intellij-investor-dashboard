package com.vermouthx.stocker.views.dialogs

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.vermouthx.stocker.StockerAppManager
import com.vermouthx.stocker.entities.StockerSuggestion
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.enums.StockerStockOperation
import com.vermouthx.stocker.settings.StockerSetting
import com.vermouthx.stocker.utils.StockerActionUtil
import com.vermouthx.stocker.utils.StockerPinyinUtil
import com.vermouthx.stocker.utils.StockerSuggestHttpUtil
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.swing.Action
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent

class StockerSuggestionDialog(val project: Project?) : DialogWrapper(project) {

    private val log = Logger.getInstance(StockerSuggestionDialog::class.java)
    private val service: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private val setting = StockerSetting.instance

    private var suggestions: List<StockerSuggestion> = emptyList()
    private var searchTask: ScheduledFuture<*>? = null
    private var isLoading: Boolean = false
    private var searchMode: SearchMode = SearchMode.STOCKS

    private enum class SearchMode(val displayName: String) {
        STOCKS("Stocks (CN/HK/US)"),
        CRYPTO("Crypto")
    }

    init {
        title = "Search Assets"
        init()
    }

    override fun createCenterPanel(): DialogPanel {
        val dialogPanel = DialogPanel(BorderLayout())
        val searchTextField = SearchTextField(true)
        val scrollPane = JBScrollPane()
        
        // Function to perform search
        val performSearch = { text: String ->
            // Cancel any pending search task
            searchTask?.cancel(false)
            
            if (text.isEmpty()) {
                isLoading = false
                suggestions = emptyList()
                SwingUtilities.invokeLater { refreshScrollPane(scrollPane) }
            } else {
                // Show loading state immediately
                isLoading = true
                SwingUtilities.invokeLater { refreshScrollPane(scrollPane) }
                
                // Debounce: schedule search after 300ms delay
                searchTask = service.schedule({
                    try {
                        // Use appropriate provider and filter based on search mode
                        val (provider, marketTypeFilter) = if (searchMode == SearchMode.CRYPTO) {
                            setting.cryptoQuoteProvider to setOf(StockerMarketType.Crypto)
                        } else {
                            setting.quoteProvider to setOf(
                                StockerMarketType.AShare,
                                StockerMarketType.HKStocks,
                                StockerMarketType.USStocks
                            )
                        }
                        
                        // Fetch filtered suggestions
                        val filteredSuggestions = StockerSuggestHttpUtil.suggest(text, provider, marketTypeFilter)
                        
                        // Update UI on EDT
                        SwingUtilities.invokeLater {
                            isLoading = false
                            suggestions = filteredSuggestions
                            refreshScrollPane(scrollPane)
                        }
                    } catch (e: Exception) {
                        log.warn("Failed to fetch suggestions", e)
                        SwingUtilities.invokeLater {
                            isLoading = false
                            refreshScrollPane(scrollPane)
                        }
                    }
                }, 300, TimeUnit.MILLISECONDS)
            }
        }
        
        // Create mode selector panel
        val modePanel = JPanel(FlowLayout(FlowLayout.LEFT))
        modePanel.add(JLabel("Search for:"))
        val modeComboBox = ComboBox(arrayOf(SearchMode.STOCKS.displayName, SearchMode.CRYPTO.displayName))
        modeComboBox.selectedIndex = 0
        modeComboBox.addActionListener {
            searchMode = when (modeComboBox.selectedIndex) {
                0 -> SearchMode.STOCKS
                1 -> SearchMode.CRYPTO
                else -> SearchMode.STOCKS
            }
            // Trigger search again with new mode if there's text
            val text = searchTextField.text.trim()
            if (text.isNotEmpty()) {
                performSearch(text)
            }
        }
        modePanel.add(modeComboBox)

        searchTextField.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                val text = searchTextField.text.trim()
                performSearch(text)
            }
        })

        // Initialize with empty state instead of hardcoded search
        refreshScrollPane(scrollPane)

        // Create top panel with mode selector and search field
        val topPanel = JPanel(BorderLayout())
        topPanel.add(modePanel, BorderLayout.NORTH)
        searchTextField.border = BorderFactory.createEmptyBorder(8, 0, 8, 0)
        topPanel.add(searchTextField, BorderLayout.CENTER)
        
        dialogPanel.add(topPanel, BorderLayout.NORTH)
        dialogPanel.add(scrollPane, BorderLayout.CENTER)
        dialogPanel.preferredSize = Dimension(550, 500)
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
        val contentPanel = if (isLoading) {
            panel {
                row {
                    label("Searching...").align(AlignX.CENTER)
                }
            }.withBorder(BorderFactory.createEmptyBorder(16, 8, 8, 8))
        } else if (suggestions.isEmpty()) {
            val message = when (searchMode) {
                SearchMode.STOCKS -> "Type to search for stocks (CN/HK/US)..."
                SearchMode.CRYPTO -> "Type to search for crypto..."
            }
            panel {
                row {
                    label(message).align(AlignX.CENTER)
                }
            }.withBorder(BorderFactory.createEmptyBorder(16, 8, 8, 8))
        } else {
            panel {
                // Add header row
                row {
                    label("Code").bold()
                        .applyToComponent {
                            minimumSize = java.awt.Dimension(100, 0)
                            preferredSize = java.awt.Dimension(100, preferredSize.height)
                        }
                    label("Name").bold()
                        .applyToComponent {
                            minimumSize = java.awt.Dimension(300, 0)
                            preferredSize = java.awt.Dimension(300, preferredSize.height)
                        }
                    label("Action").bold()
                        .applyToComponent {
                            minimumSize = java.awt.Dimension(100, 0)
                            preferredSize = java.awt.Dimension(100, preferredSize.height)
                        }
                }.bottomGap(com.intellij.ui.dsl.builder.BottomGap.SMALL)
                
                separator()
                
                // Add suggestion rows
                suggestions.forEach { suggestion ->
                    val actionButton = JButton()
                    val displayName = setting.getDisplayName(suggestion.code, suggestion.name)
                    
                    row {
                        label(suggestion.code)
                            .applyToComponent {
                                minimumSize = java.awt.Dimension(100, 0)
                                preferredSize = java.awt.Dimension(100, preferredSize.height)
                            }
                        label(
                            if (displayName.length <= 30) {
                                displayName
                            } else {
                                "${displayName.substring(0, 30)}..."
                            }
                        ).applyToComponent {
                            minimumSize = java.awt.Dimension(300, 0)
                            preferredSize = java.awt.Dimension(300, preferredSize.height)
                        }
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
                        cell(actionButton)
                            .applyToComponent {
                                minimumSize = java.awt.Dimension(100, 0)
                                preferredSize = java.awt.Dimension(100, preferredSize.height)
                            }
                    }.bottomGap(com.intellij.ui.dsl.builder.BottomGap.SMALL)
                }
            }.withBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8))
        }
        
        scrollPane.setViewportView(contentPanel)
        // Force UI refresh to prevent flickering
        scrollPane.revalidate()
        scrollPane.repaint()
    }

}
