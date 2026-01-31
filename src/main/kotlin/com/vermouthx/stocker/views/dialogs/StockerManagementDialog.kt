package com.vermouthx.stocker.views.dialogs

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.AlignY
import com.intellij.ui.dsl.builder.panel
import com.vermouthx.stocker.StockerAppManager
import com.vermouthx.stocker.entities.StockerQuote
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.settings.StockerSetting
import com.vermouthx.stocker.utils.StockerPinyinUtil
import com.vermouthx.stocker.utils.StockerQuoteHttpUtil
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.util.concurrent.CompletableFuture
import javax.swing.*

class StockerManagementDialog(val project: Project?) : DialogWrapper(project) {

    private val log = Logger.getInstance(StockerManagementDialog::class.java)
    private val setting = StockerSetting.instance

    private val tabMap: MutableMap<StockerMarketType, JPanel> = mutableMapOf()

    private val currentSymbols: MutableMap<StockerMarketType, DefaultListModel<StockerQuote>> = mutableMapOf()

    private var currentMarketSelection: StockerMarketType = StockerMarketType.AShare

    init {
        title = "Manage Favorite Stocks"
        init()
    }

    override fun createCenterPanel(): DialogPanel {
        val tabbedPane = JBTabbedPane()
        tabbedPane.add("CN", createTabContent(StockerMarketType.AShare))
        tabbedPane.add("HK", createTabContent(StockerMarketType.HKStocks))
        tabbedPane.add("US", createTabContent(StockerMarketType.USStocks))
        tabbedPane.add("Crypto", createTabContent(StockerMarketType.Crypto))
        
        tabbedPane.addChangeListener {
            currentMarketSelection = when (tabbedPane.selectedIndex) {
                0 -> StockerMarketType.AShare
                1 -> StockerMarketType.HKStocks
                2 -> StockerMarketType.USStocks
                3 -> StockerMarketType.Crypto
                else -> return@addChangeListener
            }
        }

        // Load data asynchronously for each market type
        loadMarketData(StockerMarketType.AShare, setting.aShareList)
        loadMarketData(StockerMarketType.HKStocks, setting.hkStocksList)
        loadMarketData(StockerMarketType.USStocks, setting.usStocksList)
        loadMarketData(StockerMarketType.Crypto, setting.cryptoList)

        tabbedPane.selectedIndex = 0
        return panel {
            row {
                cell(tabbedPane).align(AlignX.FILL)
            }
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
                listModel.addAll(quotes)
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
                    label("Loading...").align(AlignX.CENTER)
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
        return panel {
            row {
                cell(pane).align(AlignX.FILL).align(AlignY.FILL)
            }
        }
    }

    private fun renderTabPane(pane: JPanel, listModel: DefaultListModel<StockerQuote>) {
        // Clear existing components to prevent stacking
        pane.removeAll()
        
        val list = JBList(listModel)
        list.installCellRenderer { symbol ->
            // Get original name with Pinyin if enabled
            val originalName = if (setting.displayNameWithPinyin) {
                StockerPinyinUtil.toPinyin(symbol.name)
            } else {
                symbol.name
            }
            
            // Get custom name if exists
            val customName = setting.getCustomName(symbol.code)
            
            panel {
                row {
                    label(symbol.code)
                        .applyToComponent { 
                            minimumSize = java.awt.Dimension(80, 0)
                            preferredSize = java.awt.Dimension(80, preferredSize.height)
                        }
                    label(
                        if (originalName.length <= 25) {
                            originalName
                        } else {
                            "${originalName.substring(0, 25)}..."
                        }
                    ).applyToComponent {
                        minimumSize = java.awt.Dimension(200, 0)
                        preferredSize = java.awt.Dimension(200, preferredSize.height)
                    }
                    label(
                        customName?.let {
                            if (it.length <= 25) {
                                it
                            } else {
                                "${it.substring(0, 25)}..."
                            }
                        } ?: "-"
                    ).applyToComponent {
                        minimumSize = java.awt.Dimension(200, 0)
                        preferredSize = java.awt.Dimension(200, preferredSize.height)
                    }
                }
            }.withBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8))
        }
        
        // Create header panel
        val headerPanel = panel {
            row {
                label("Code").bold()
                    .applyToComponent {
                        minimumSize = java.awt.Dimension(80, 0)
                        preferredSize = java.awt.Dimension(80, preferredSize.height)
                    }
                label("Original Name").bold()
                    .applyToComponent {
                        minimumSize = java.awt.Dimension(200, 0)
                        preferredSize = java.awt.Dimension(200, preferredSize.height)
                    }
                label("Custom Name").bold()
                    .applyToComponent {
                        minimumSize = java.awt.Dimension(200, 0)
                        preferredSize = java.awt.Dimension(200, preferredSize.height)
                    }
            }
        }.withBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, javax.swing.UIManager.getColor("Separator.foreground")),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ))
        
        // ToolbarDecorator.createPanel() already includes the list with scrolling
        val decorator = ToolbarDecorator.createDecorator(list)
            .setEditAction { button ->
                val selectedIndex = list.selectedIndex
                if (selectedIndex >= 0) {
                    val selectedQuote = listModel.getElementAt(selectedIndex)
                    val currentCustomName = setting.getCustomName(selectedQuote.code)
                    val newName = JOptionPane.showInputDialog(
                        pane,
                        "Enter custom name for ${selectedQuote.code}:",
                        currentCustomName ?: selectedQuote.name
                    )
                    if (newName != null && newName.isNotBlank()) {
                        setting.setCustomName(selectedQuote.code, newName.trim())
                        // Trigger list repaint to show new name
                        list.repaint()
                    } else if (newName != null && newName.isBlank() && currentCustomName != null) {
                        // If user clears the name, remove custom name
                        setting.removeCustomName(selectedQuote.code)
                        list.repaint()
                    }
                }
            }
            .setEditActionUpdater { list.selectedIndex >= 0 }
        
        val decoratedPanel = decorator.createPanel()
        
        // Add header at top and decorated panel (which contains toolbar + list + scrollpane) below
        pane.add(headerPanel, BorderLayout.NORTH)
        pane.add(decoratedPanel, BorderLayout.CENTER)
        
        // Refresh the UI to show new components
        pane.revalidate()
        pane.repaint()
    }

}
