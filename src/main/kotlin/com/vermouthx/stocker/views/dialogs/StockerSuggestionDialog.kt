package com.vermouthx.stocker.views.dialogs

import com.intellij.CommonBundle
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.UIUtil
import com.vermouthx.stocker.StockerAppManager
import com.vermouthx.stocker.StockerBundle
import com.vermouthx.stocker.entities.StockerSuggestion
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.settings.StockerSetting
import com.vermouthx.stocker.utils.StockerActionUtil
import com.vermouthx.stocker.utils.StockerSuggestHttpUtil
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.KeyEvent
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.swing.Action
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.KeyStroke
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent

class StockerSuggestionDialog(val project: Project?) : DialogWrapper(project) {

    companion object {
        private const val CODE_WIDTH = 90
        private const val MARKET_WIDTH = 55
        private const val NAME_WIDTH = 240
        private const val ACTION_WIDTH = 90
        private const val NAME_TRIM_LENGTH = 25
    }

    private val log = Logger.getInstance(StockerSuggestionDialog::class.java)
    private val service: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private val setting = StockerSetting.instance

    private var suggestions: List<StockerSuggestion> = emptyList()
    private var searchTask: ScheduledFuture<*>? = null
    private var isLoading: Boolean = false
    private var searchMode: SearchMode = SearchMode.STOCKS

    // Suppress the platform's action-system Esc shortcut (ACTION_CLEAR_TEXT): it intercepts
    // Esc ahead of every Swing binding. Our own binding below restores clear-then-close.
    private val searchTextField = object : SearchTextField(true) {
        override fun toClearTextOnEscape(): Boolean = false
    }
    private lateinit var headerPanel: JPanel

    private enum class SearchMode(val displayNameKey: String, val hintKey: String) {
        STOCKS("dialog.search.mode.stocks", "dialog.search.hint.stocks"),
        CRYPTO("dialog.search.mode.crypto", "dialog.search.hint.crypto")
    }

    init {
        title = StockerBundle.message("dialog.search.title")
        setCancelButtonText(CommonBundle.getCloseButtonText())
        init()
    }

    override fun getPreferredFocusedComponent(): JComponent = searchTextField

    // A single Close button; also restores Esc-to-close, which an empty action list disabled.
    override fun createActions(): Array<Action> = arrayOf(cancelAction)

    override fun createCenterPanel(): DialogPanel {
        val dialogPanel = DialogPanel(BorderLayout())
        val scrollPane = JBScrollPane()
        scrollPane.border = BorderFactory.createEmptyBorder()

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

                        val filteredSuggestions = StockerSuggestHttpUtil.suggest(text, provider, marketTypeFilter)

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

        val modePanel = JPanel(FlowLayout(FlowLayout.LEFT, 8, 0))
        modePanel.add(JLabel(StockerBundle.message("dialog.search.mode.label")))
        val modeComboBox = ComboBox(SearchMode.entries.map { StockerBundle.message(it.displayNameKey) }.toTypedArray())
        modeComboBox.selectedIndex = 0
        modeComboBox.addActionListener {
            searchMode = SearchMode.entries.getOrElse(modeComboBox.selectedIndex) { SearchMode.STOCKS }
            val text = searchTextField.text.trim()
            if (text.isNotEmpty()) {
                performSearch(text)
            } else {
                refreshScrollPane(scrollPane)
            }
        }
        modePanel.add(modeComboBox)

        searchTextField.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                performSearch(searchTextField.text.trim())
            }
        })
        // SearchTextField consumes Esc before the dialog's cancel binding sees it.
        // Restore the platform search convention: Esc clears the query first, then closes.
        searchTextField.textEditor.registerKeyboardAction(
            {
                if (searchTextField.text.isNotEmpty()) {
                    searchTextField.text = ""
                } else {
                    doCancelAction()
                }
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_FOCUSED
        )

        // Fixed result header: stays put while the result list scrolls beneath it.
        headerPanel = panel {
            row {
                label(StockerBundle.message("column.symbol")).bold().fixedWidth(CODE_WIDTH)
                label(StockerBundle.message("dialog.search.column.market")).bold().fixedWidth(MARKET_WIDTH)
                label(StockerBundle.message("column.name")).bold().fixedWidth(NAME_WIDTH)
                label(StockerBundle.message("dialog.search.column.action")).bold().fixedWidth(ACTION_WIDTH)
            }
        }.withBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8))

        val topPanel = JPanel(BorderLayout())
        topPanel.add(modePanel, BorderLayout.NORTH)
        searchTextField.border = BorderFactory.createEmptyBorder(8, 0, 4, 0)
        topPanel.add(searchTextField, BorderLayout.CENTER)
        topPanel.add(headerPanel, BorderLayout.SOUTH)

        refreshScrollPane(scrollPane)

        dialogPanel.add(topPanel, BorderLayout.NORTH)
        dialogPanel.add(scrollPane, BorderLayout.CENTER)
        dialogPanel.preferredSize = Dimension(560, 500)
        return dialogPanel
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
        } finally {
            StockerSuggestHttpUtil.closeConnections()
        }
        super.dispose()
    }

    private fun <T : JComponent> Cell<T>.fixedWidth(width: Int): Cell<T> = applyToComponent {
        minimumSize = Dimension(width, 0)
        preferredSize = Dimension(width, preferredSize.height)
    }

    private fun refreshScrollPane(scrollPane: JBScrollPane) {
        headerPanel.isVisible = !isLoading && suggestions.isNotEmpty()
        val contentPanel = if (isLoading) {
            panel {
                row {
                    label(StockerBundle.message("dialog.search.searching"))
                        .align(AlignX.CENTER)
                        .applyToComponent {
                            icon = AnimatedIcon.Default()
                            iconTextGap = 8
                        }
                }
            }.withBorder(BorderFactory.createEmptyBorder(16, 8, 8, 8))
        } else if (suggestions.isEmpty()) {
            panel {
                row {
                    label(StockerBundle.message(searchMode.hintKey))
                        .align(AlignX.CENTER)
                        .applyToComponent { foreground = UIUtil.getContextHelpForeground() }
                }
            }.withBorder(BorderFactory.createEmptyBorder(16, 8, 8, 8))
        } else {
            panel {
                suggestions.forEach { suggestion ->
                    row {
                        label(suggestion.code).fixedWidth(CODE_WIDTH)
                        label(suggestion.market.title)
                            .fixedWidth(MARKET_WIDTH)
                            .applyToComponent { foreground = UIUtil.getContextHelpForeground() }
                        val displayName = setting.getDisplayName(suggestion.code, suggestion.name)
                        label(
                            if (displayName.length <= NAME_TRIM_LENGTH) {
                                displayName
                            } else {
                                "${displayName.take(NAME_TRIM_LENGTH)}…"
                            }
                        ).fixedWidth(NAME_WIDTH)
                            .applyToComponent { toolTipText = displayName }
                        cell(createOperationButton(suggestion)).fixedWidth(ACTION_WIDTH)
                    }.bottomGap(BottomGap.SMALL)
                }
            }.withBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8))
        }

        scrollPane.setViewportView(contentPanel)
        // Force UI refresh to prevent flickering
        scrollPane.revalidate()
        scrollPane.repaint()
    }

    /**
     * The button label always reflects the actual watchlist state, so a rejected add
     * (validation failure) keeps showing "Add" instead of blindly flipping to "Delete".
     */
    private fun createOperationButton(suggestion: StockerSuggestion): JButton {
        val actionButton = JButton()
        fun refreshButtonText() {
            actionButton.text = if (setting.containsCode(suggestion.code)) {
                StockerBundle.message("operation.delete")
            } else {
                StockerBundle.message("operation.add")
            }
        }
        refreshButtonText()
        actionButton.addActionListener {
            val myApplication = StockerAppManager.myApplication(project) ?: return@addActionListener
            myApplication.shutdownThenClear()
            if (setting.containsCode(suggestion.code)) {
                StockerActionUtil.removeStock(suggestion.market, suggestion)
            } else {
                StockerActionUtil.addStock(suggestion.market, suggestion, project)
            }
            refreshButtonText()
            myApplication.schedule()
        }
        return actionButton
    }

}
