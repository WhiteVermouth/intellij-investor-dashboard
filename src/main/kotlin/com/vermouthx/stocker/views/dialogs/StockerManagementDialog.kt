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
        
        tabbedPane.addChangeListener {
            currentMarketSelection = when (tabbedPane.selectedIndex) {
                0 -> StockerMarketType.AShare
                1 -> StockerMarketType.HKStocks
                2 -> StockerMarketType.USStocks
                else -> return@addChangeListener
            }
        }

        // Load data asynchronously for each market type
        loadMarketData(StockerMarketType.AShare, setting.aShareList)
        loadMarketData(StockerMarketType.HKStocks, setting.hkStocksList)
        loadMarketData(StockerMarketType.USStocks, setting.usStocksList)

        tabbedPane.selectedIndex = 0
        return panel {
            row {
                cell(tabbedPane).align(AlignX.FILL)
            }
        }.withPreferredWidth(300)
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
                StockerQuoteHttpUtil.get(marketType, setting.quoteProvider, codes)
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
            panel {
                row {
                    label(symbol.code).align(AlignX.LEFT)
                    label(
                        if (symbol.name.length <= 20) {
                            symbol.name
                        } else {
                            "${symbol.name.substring(0, 20)}..."
                        }
                    ).align(AlignX.CENTER)
                }
            }.withBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16))
        }
        
        // ToolbarDecorator.createPanel() already includes the list with scrolling
        val decorator = ToolbarDecorator.createDecorator(list)
        val decoratedPanel = decorator.createPanel()
        
        // Add only the decorated panel (which contains toolbar + list + scrollpane)
        pane.add(decoratedPanel, BorderLayout.CENTER)
        
        // Refresh the UI to show new components
        pane.revalidate()
        pane.repaint()
    }

}
