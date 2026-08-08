package com.vermouthx.stocker.views.dialogs

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.panel
import com.vermouthx.stocker.StockerAppManager
import com.vermouthx.stocker.StockerBundle
import com.vermouthx.stocker.entities.StockerQuote
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.settings.StockerSetting
import com.vermouthx.stocker.utils.StockerPinyinUtil
import com.vermouthx.stocker.utils.StockerQuoteHttpUtil
import com.vermouthx.stocker.views.StockerTableView
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.util.concurrent.CompletableFuture
import javax.swing.*

class StockerManagementDialog(val project: Project?) : DialogWrapper(project) {

    companion object {
        private const val CODE_WIDTH = 80
        private const val ORIGINAL_NAME_WIDTH = 150
        private const val CUSTOM_NAME_WIDTH = 120
        private const val COST_WIDTH = 80
        private const val HOLDINGS_WIDTH = 80
        private const val NAME_TRIM_LENGTH = 25
        private const val CUSTOM_NAME_TRIM_LENGTH = 15
    }

    private val log = Logger.getInstance(StockerManagementDialog::class.java)
    private val setting = StockerSetting.instance

    private val tabMap: MutableMap<StockerMarketType, JPanel> = mutableMapOf()

    private val currentSymbols: MutableMap<StockerMarketType, DefaultListModel<StockerQuote>> = mutableMapOf()

    init {
        title = StockerBundle.message("dialog.manage.title")
        init()
    }

    override fun createCenterPanel(): DialogPanel {
        val tabbedPane = JBTabbedPane()
        val markets = listOf(
            StockerMarketType.AShare,
            StockerMarketType.HKStocks,
            StockerMarketType.USStocks,
            StockerMarketType.Crypto
        )
        markets.forEach { market ->
            tabbedPane.add(market.title, createTabContent(market))
        }

        // Load data asynchronously for each market type
        loadMarketData(StockerMarketType.AShare, setting.aShareList)
        loadMarketData(StockerMarketType.HKStocks, setting.hkStocksList)
        loadMarketData(StockerMarketType.USStocks, setting.usStocksList)
        loadMarketData(StockerMarketType.Crypto, setting.cryptoList)

        tabbedPane.selectedIndex = 0
        return panel {
            row {
                cell(tabbedPane).align(Align.FILL)
            }.resizableRow()
        }.withPreferredWidth(600).withPreferredHeight(400)
    }

    private fun loadMarketData(marketType: StockerMarketType, codes: List<String>) {
        val listModel = DefaultListModel<StockerQuote>()
        currentSymbols[marketType] = listModel

        // Show loading state
        tabMap[marketType]?.let { pane ->
            showLoadingState(pane)
        }

        CompletableFuture.supplyAsync {
            try {
                // Use cryptoQuoteProvider for crypto, quoteProvider for stocks
                val provider = if (marketType == StockerMarketType.Crypto) {
                    setting.cryptoQuoteProvider
                } else {
                    setting.quoteProvider
                }
                StockerQuoteHttpUtil.get(marketType, provider, codes)
            } catch (e: Exception) {
                log.warn("Failed to load quotes for market type $marketType", e)
                emptyList()
            }
        }.thenAccept { quotes ->
            SwingUtilities.invokeLater {
                // Keep every stored code visible even when its quote could not be fetched.
                // Pressing OK rebuilds the watchlist from this model, so dropping a row
                // here would silently delete the favorite (e.g. while offline).
                val quotesByCode = quotes.associateBy { it.code.uppercase() }
                codes.forEach { code ->
                    val quote = quotesByCode[code.uppercase()]
                        ?: StockerQuote(
                            code = code, name = code,
                            current = 0.0, opening = 0.0, close = 0.0,
                            low = 0.0, high = 0.0, change = 0.0, percentage = 0.0,
                            updateAt = ""
                        )
                    listModel.addElement(quote)
                }
                tabMap[marketType]?.let { pane ->
                    renderTabPane(pane, listModel)
                }
            }
        }
    }

    private fun showLoadingState(pane: JPanel) {
        pane.removeAll()
        pane.add(
            panel {
                row {
                    label(StockerBundle.message("table.empty.loading"))
                        .align(AlignX.CENTER)
                        .applyToComponent {
                            icon = AnimatedIcon.Default()
                            iconTextGap = 8
                        }
                }
            }, BorderLayout.CENTER
        )
        pane.revalidate()
        pane.repaint()
    }

    override fun createActions(): Array<Action> {
        return arrayOf(
            object : OkAction() {
                override fun actionPerformed(e: ActionEvent?) {
                    val myApplication = StockerAppManager.myApplication(project)
                    if (myApplication != null) {
                        myApplication.shutdownThenClear()
                        currentSymbols[StockerMarketType.AShare]?.let { symbols ->
                            setting.aShareList = symbols.elements().asSequence().map { it.code }.toMutableList()
                        }
                        currentSymbols[StockerMarketType.HKStocks]?.let { symbols ->
                            setting.hkStocksList = symbols.elements().asSequence().map { it.code }.toMutableList()
                        }
                        currentSymbols[StockerMarketType.USStocks]?.let { symbols ->
                            setting.usStocksList = symbols.elements().asSequence().map { it.code }.toMutableList()
                        }
                        currentSymbols[StockerMarketType.Crypto]?.let { symbols ->
                            setting.cryptoList = symbols.elements().asSequence().map { it.code }.toMutableList()
                        }
                        myApplication.schedule()
                    }
                    super.actionPerformed(e)
                }
            }, cancelAction
        )
    }

    private fun createTabContent(marketType: StockerMarketType): JComponent {
        val pane = JPanel(BorderLayout())
        tabMap[marketType] = pane
        return pane
    }

    private fun <T : JComponent> Cell<T>.fixedWidth(width: Int): Cell<T> = applyToComponent {
        minimumSize = Dimension(width, 0)
        preferredSize = Dimension(width, preferredSize.height)
    }

    private fun <T : JComponent> Cell<T>.withForeground(color: Color): Cell<T> = applyToComponent {
        foreground = color
    }

    private fun renderTabPane(pane: JPanel, listModel: DefaultListModel<StockerQuote>) {
        // Clear existing components to prevent stacking
        pane.removeAll()

        val list = JBList(listModel)
        list.setEmptyText(StockerBundle.message("dialog.manage.empty"))
        list.cellRenderer = ListCellRenderer<StockerQuote> { jList, symbol, _, isSelected, _ ->
            // Get original name with Pinyin if enabled
            val originalName = if (setting.displayNameWithPinyin) {
                StockerPinyinUtil.toPinyin(symbol.name)
            } else {
                symbol.name
            }

            // Get custom name if exists
            val customName = setting.getCustomName(symbol.code)
            val costPrice = setting.getCostPrice(symbol.code)
            val holdings = setting.getHoldings(symbol.code)

            val textColor = if (isSelected) jList.selectionForeground else jList.foreground

            panel {
                row {
                    label(symbol.code).fixedWidth(CODE_WIDTH).withForeground(textColor)
                    label(
                        if (originalName.length <= NAME_TRIM_LENGTH) {
                            originalName
                        } else {
                            "${originalName.take(NAME_TRIM_LENGTH)}…"
                        }
                    ).fixedWidth(ORIGINAL_NAME_WIDTH).withForeground(textColor)
                    label(
                        customName?.let {
                            if (it.length <= CUSTOM_NAME_TRIM_LENGTH) {
                                it
                            } else {
                                "${it.take(CUSTOM_NAME_TRIM_LENGTH)}…"
                            }
                        } ?: "-"
                    ).fixedWidth(CUSTOM_NAME_WIDTH).withForeground(textColor)
                    label(
                        costPrice?.let { String.format("%.3f", it) } ?: "-"
                    ).fixedWidth(COST_WIDTH).withForeground(textColor)
                    label(
                        holdings?.toString() ?: "-"
                    ).fixedWidth(HOLDINGS_WIDTH).withForeground(textColor)
                }
            }.apply {
                // JList paints nothing behind the renderer, so this panel has to carry the
                // selection colours itself — otherwise the selected row looks unselected.
                isOpaque = true
                background = if (isSelected) jList.selectionBackground else jList.background
                border = BorderFactory.createEmptyBorder(4, 8, 4, 8)
            }
        }

        // Column titles for this faux-table list. Left padding matches the cell renderer's so
        // the titles line up with the values underneath. Deliberately no bottom rule: the
        // toolbar below already carries ToolbarDecorator's own border, and a second line here
        // read as a stray divider boxing the toolbar in.
        val headerPanel = panel {
            row {
                label(StockerBundle.message("column.symbol")).bold().fixedWidth(CODE_WIDTH)
                label(StockerBundle.message("dialog.manage.column.original.name")).bold().fixedWidth(ORIGINAL_NAME_WIDTH)
                label(StockerBundle.message("dialog.manage.column.custom.name")).bold().fixedWidth(CUSTOM_NAME_WIDTH)
                label(StockerBundle.message("column.cost.price")).bold().fixedWidth(COST_WIDTH)
                label(StockerBundle.message("column.holdings")).bold().fixedWidth(HOLDINGS_WIDTH)
            }
        }.withBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8))

        // ToolbarDecorator.createPanel() already includes the list with scrolling
        val decorator = ToolbarDecorator.createDecorator(list)
            .setEditAction {
                val selectedIndex = list.selectedIndex
                if (selectedIndex >= 0) {
                    editFavorite(pane, list, listModel.getElementAt(selectedIndex))
                }
            }
            .setEditActionUpdater { list.selectedIndex >= 0 }

        val decoratedPanel = decorator.createPanel()

        // Column titles on top, toolbar + list below.
        pane.add(headerPanel, BorderLayout.NORTH)
        pane.add(decoratedPanel, BorderLayout.CENTER)

        // Refresh the UI to show new components
        pane.revalidate()
        pane.repaint()
    }

    private fun editFavorite(parent: JComponent, list: JBList<StockerQuote>, quote: StockerQuote) {
        val currentCustomName = setting.getCustomName(quote.code)
        val currentCostPrice = setting.getCostPrice(quote.code)
        val currentHoldings = setting.getHoldings(quote.code)

        val dialog = EditFavoriteDialog(parent, quote.code, currentCustomName, currentCostPrice, currentHoldings)
        if (!dialog.showAndGet()) {
            return
        }

        // Handle custom name
        val newName = dialog.customName
        if (newName.isNotBlank()) {
            setting.setCustomName(quote.code, newName)
        } else if (currentCustomName != null) {
            setting.removeCustomName(quote.code)
        }

        // Handle cost price (validated by the dialog)
        val costPrice = dialog.costPrice
        if (costPrice != null) {
            setting.setCostPrice(quote.code, costPrice)
        } else if (currentCostPrice != null) {
            setting.removeCostPrice(quote.code)
        }

        // Handle holdings (validated by the dialog)
        val holdings = dialog.holdings
        if (holdings != null) {
            setting.setHoldings(quote.code, holdings)
        } else if (currentHoldings != null) {
            setting.removeHoldings(quote.code)
        }

        StockerTableView.refreshAllFinancialColumns()
        list.repaint()
    }

    /**
     * Platform-styled replacement for the former JOptionPane popup: proper title,
     * Esc/Enter handling, and inline validation of the numeric fields.
     */
    private class EditFavoriteDialog(
        parent: Component,
        code: String,
        initialCustomName: String?,
        initialCostPrice: Double?,
        initialHoldings: Int?
    ) : DialogWrapper(parent, false) {

        private val nameField = JBTextField(initialCustomName ?: "", 20)
        private val costPriceField = JBTextField(initialCostPrice?.let { String.format("%.3f", it) } ?: "", 20)
        private val holdingsField = JBTextField(initialHoldings?.toString() ?: "", 20)

        val customName: String get() = nameField.text.trim()
        val costPrice: Double? get() = costPriceField.text.trim().toDoubleOrNull()
        val holdings: Int? get() = holdingsField.text.trim().toIntOrNull()

        init {
            title = StockerBundle.message("dialog.manage.edit.title", code)
            init()
        }

        override fun getPreferredFocusedComponent(): JComponent = nameField

        override fun createCenterPanel(): JComponent = panel {
            row(StockerBundle.message("dialog.manage.edit.custom.name")) {
                cell(nameField).align(AlignX.FILL)
            }
            row(StockerBundle.message("dialog.manage.edit.cost.price")) {
                cell(costPriceField).align(AlignX.FILL)
            }
            row(StockerBundle.message("dialog.manage.edit.holdings")) {
                cell(holdingsField).align(AlignX.FILL)
            }
        }

        override fun doValidate(): ValidationInfo? {
            val costText = costPriceField.text.trim()
            if (costText.isNotEmpty() && costText.toDoubleOrNull() == null) {
                return ValidationInfo(StockerBundle.message("dialog.manage.edit.invalid.number"), costPriceField)
            }
            val holdingsText = holdingsField.text.trim()
            if (holdingsText.isNotEmpty() && holdingsText.toIntOrNull() == null) {
                return ValidationInfo(StockerBundle.message("dialog.manage.edit.invalid.integer"), holdingsField)
            }
            return null
        }
    }

}
